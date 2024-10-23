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
import static java.util.stream.Gatherers.fold;
import static java.util.stream.Gatherers.scan;
import static java.util.stream.Gatherers.windowSliding;

record Commit(int index, String from, String date, String subject, String blank) {}
record Result(int total, boolean green) {}

// java CommitHeaderChecker.java https://github.com/apache/netbeans/pull/${{ github.event.pull_request.number }}
void main(String[] args) throws IOException, InterruptedException {

    if (args.length != 1 || !args[0].startsWith("https://github.com/")) {
        throw new IllegalArgumentException("PR URL expected");
    }

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(args[0]+".patch"))
            .timeout(Duration.ofSeconds(10))
            .build();

    println("checking commit(s)...");
    Result result;
    try (HttpClient client = HttpClient.newBuilder()
            .followRedirects(Redirect.NORMAL).build()) {

        result = client.send(request, BodyHandlers.ofLines()).body()
            .gather(windowSliding(4))
            .filter(w -> w.size() == 4 && w.get(0).startsWith("From: ") && w.get(1).startsWith("Date: ") && w.get(2).startsWith("Subject: "))
            .gather(scan(
                () -> new Commit(0, "", "", "", ""),
                (c, w) -> new Commit(c.index+1, w.get(0), w.get(1), w.get(2), w.get(3))))
            .gather(fold(
                () -> new Result(0, true),
                (r, c) -> new Result(r.total+1, r.green & checkCommit(c))))
            .findFirst()
            .orElseThrow();
    }

    println(result.total + " commit(s) checked");
    System.exit(result.green ? 0 : 1);
}

boolean checkCommit(Commit c) {
    return checkNameAndEmail(c.from, c.index)
         & checkSubject(c.subject, c.index)
         & checkBlankLineAfterSubject(c.blank, c.index);
}

boolean checkNameAndEmail(String from, int i) {
    // From: Duke <duke42@dukemail.com>
    int start = from.indexOf('<');
    int end = from.indexOf('>');

    String mail = end > start ? from.substring(start+1, end) : "";
    String author = start > 6 ? from.substring(6, start).strip() : "";

    boolean green = true;
    if (mail.isBlank() || !mail.contains("@") || mail.contains("noreply") || mail.contains("localhost")) {
        println("::error::invalid email in commit " + i + " '" + from + "'");
        green = false;
    }
    // single word author -> probably the nickname/account name/root etc
    // mime encoded is probably fine, since gh account names can't be encoded
    if (author.isBlank() || (!author.contains(" ") && !(author.startsWith("=?") && author.endsWith("?=")))) {
        println("::error::invalid author in commit " + i + " '" + author + "' (full name?)");
        green = false;
    }
    return green;
}

// https://mirrors.edge.kernel.org/pub/software/scm/git/docs/git-commit.html#_discussion
boolean checkSubject(String subject, int i) {
    // Subject: [PATCH] msg
    subject = subject.substring(subject.indexOf(']')+1).strip();
    // single word subjects are likely not intended or should be squashed
    if (!subject.contains(" ")) {
        println("::error::invalid subject in commit " + i + " '" + subject + "'");
        return false;
    }
    return true;
}

boolean checkBlankLineAfterSubject(String blank, int i) {
    if (!blank.isBlank()) {
        println("::warning::blank line after subject recommended in commit " + i + " (subject over 50 char limit?)");
//        return false;
    }
    return true;
}
