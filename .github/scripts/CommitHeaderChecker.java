/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;

import static java.util.stream.Gatherers.fold;
import static java.util.stream.Gatherers.scan;

static final Pattern robots = Pattern.compile("(gpt|claude|gemini|llama|mistral|command|grok|qwen|deepseek)");

record Commit(int index, String from, String date, String subject, List<String> msg) {}
record Result(int total, boolean green) {}

// checks commit headers for valid author, email and commit msg formatting
// its main purpose is to prevent common merge mistakes

// Java 25+
// java CommitHeaderChecker.java https://github.com/apache/netbeans/pull/${{ github.event.pull_request.number }}

// green tests:
// java CommitHeaderChecker.java https://github.com/apache/netbeans/pull/66
// java CommitHeaderChecker.java https://github.com/apache/netbeans/pull/7641
// java CommitHeaderChecker.java https://github.com/apache/netbeans/pull/4138
// java CommitHeaderChecker.java https://github.com/apache/netbeans/pull/4692

// red tests:
// java CommitHeaderChecker.java https://github.com/apache/netbeans/pull/7776
// java CommitHeaderChecker.java https://github.com/apache/netbeans/pull/5567

void main(String[] args) throws IOException, InterruptedException {

    if (args.length != 1 || !args[0].startsWith("https://github.com/")) {
        throw new IllegalArgumentException("PR URL expected");
    }

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(args[0]+".patch"))
            .timeout(Duration.ofSeconds(10))
            .build();

    log("checking PR patch file...");
    Result result;
    try (HttpClient client = HttpClient.newBuilder()
            .followRedirects(Redirect.NORMAL).build()) {

        result = client.send(request, BodyHandlers.ofLines()).body()
            // gather commit header + message as List of lines
            .gather(
                Gatherer.<String, List<String>, List<String>>ofSequential(
                    ArrayList::new,
                    (window, line, downstream) -> {
                        if (line.startsWith("From: ")) { // window start
                            window.add(line);
                        } else if (!window.isEmpty()) {
                            if (line.equals("---")) { // window end (separator after message)
                                downstream.push(List.copyOf(window));
                                window.clear();
                            } else {
                                window.add(line);
                            }
                        }
                        return true;
                    }
                )
            )
            .filter(w -> isCommitHeader(w))
            // map to indexed commits
            .gather(scan(
                () -> new Commit(-1, "", "", "", List.of()),
                (c, w) -> createCommit(c.index+1, w)))
            .peek(System.out::println)
            // check commits and store as result
            .gather(fold(
                () -> new Result(0, true),
                (r, c) -> new Result(r.total+1, r.green & checkCommit(c))))
            .findFirst()
            .orElseThrow();
    }

    log(result.total + " commit(s) checked");
    System.exit(result.green ? 0 : 1);
}

// From: Duke <duke42@dukemail.com>
// Date: Thu, 1 Oct 2024 22:10:50 -0700
// Subject: [PATCH] Mail Validator
private static boolean isCommitHeader(List<String> lines) {
    int i = 0;
    return lines.size() >= 4
        && lines.get(i++).startsWith("From: ") // "From" can be two lines in some cases
        &&(lines.get(i++).startsWith("Date: ") || lines.get(i++).startsWith("Date: "))
        && lines.get(i++).startsWith("Subject: ");
}

private static Commit createCommit(int index, List<String> lines) {
    int i = 0;
    return lines.get(1).startsWith("Date: ") // "From" can be two lines in some cases
      ? new Commit(index, lines.get(i++), lines.get(i++), lines.get(i++), lines.subList(i, lines.size()))
      : new Commit(index, lines.get(i++) + lines.get(i++), lines.get(i++), lines.get(i++), lines.subList(i, lines.size()));
}

boolean checkCommit(Commit c) {
    return checkNameAndEmail(c.index, c.from)
         & checkSubject(c.index, c.subject)
         & checkBlankLineAfterSubject(c.index, c.msg)
         & checkHumanCoAuthors(c.index, c.msg);
}

boolean checkNameAndEmail(int i, String from) {
    // From: Duke <duke42@dukemail.com>
    int start = from.indexOf('<');
    int end = from.indexOf('>');

    String mail = end > start ? from.substring(start+1, end) : "";
    String author = start > 6 ? from.substring(6, start).strip() : "";

    // bots may pass
    if (author.contains("[bot]")) {
        return true;
    }

    boolean green = true;
    if (mail.isBlank() || !mail.contains("@") || mail.contains("noreply") || mail.contains("localhost")) {
        log("::error::invalid email in commit " + i + " '" + from + "'");
        green = false;
    }

    // mime encoding indicates it is probably a proper name, since gh account names aren't encoded
    boolean encoded = author.startsWith("=?") && author.endsWith("?=");

    // single word author -> probably the nickname/account name/root etc
    if (author.isBlank() || (!encoded && !author.contains(" ") && !author.contains("-"))) {
        log("::error::invalid author in commit " + i + " '" + author + "' (full name?)");
        green = false;
    }
    return green;
}

// https://mirrors.edge.kernel.org/pub/software/scm/git/docs/git-commit.html#_discussion
boolean checkSubject(int i, String subject) {
    // Subject: [PATCH] msg
    subject = subject.substring(subject.indexOf(']')+1).strip();
    // single word subjects are likely not intended or should be squashed before merge
    if (!subject.contains(" ")) {
        log("::error::invalid subject in commit " + i + " '" + subject + "'");
        return false;
    }
    return true;
}

// there should be a blank line after the subject line, some subjects can overflow though.
boolean checkBlankLineAfterSubject(int i, List<String> msg) {
// disabled since this would produce too many warnings due to overflowing subject lines
//    if (!blank.isBlank()) {
//        log("::warning::blank line after subject recommended in commit " + i + " (is subject over 50 char limit?)");
// //       return false;
//    }
    return true;
}

boolean checkHumanCoAuthors(int i, List<String> msg) {
    for (String line : msg) {
        String lower = line.toLowerCase(Locale.ROOT);
        if ((lower.startsWith("co-authored-by:") || lower.startsWith("generated-by:")) && robots.matcher(lower).find()) {
            log("::error::please use 'Assisted-by: MODEL_NAME MODEL_VERSION' in commit " + i);
            return false;
        }
    }
    return true;
}

void log(String msg) {
    System.out.println(msg);
}
