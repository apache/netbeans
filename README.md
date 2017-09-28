# Apache NetBeans

Apache NetBeans is an open source development environment, tooling platform, and application framework.

### Requirements

#### Git
#### Ant 1.9.9 or above
#### JDK 8 (to build NetBeans)
#### JDK 9 (to run NetBeans)

**Note:** NetBeans also runs with JDK 8, although then it will not include tools for the JDK 9 Shell.

### Building NetBeans

Build the full project:
```
$ ant
```
Build the NetBeans Platform:
```
$ ant -Dcluster.config=platform
```

### Running NetBeans

Run the build:
```
$ ant tryme
```

**Note:** Look in nbbuild/netbeans for the NetBeans installation created by the build process.

### Get In Touch

[Subscribe](mailto:users-subscribe@netbeans.incubator.apache.org) or [mail](mailto:users@netbeans.incubator.apache.org) the [users@netbeans.incubator.apache.org](mailto:users@netbeans.incubator.apache.org) list - Ask questions, find answers, and also help other users.

[Subscribe](mailto:dev-subscribe@netbeans.incubator.apache.org) or [mail](mailto:dev@netbeans.incubator.apache.org) the [dev@netbeans.incubator.apache.org](mailto:dev@netbeans.incubator.apache.org) list - Join developement discussions, propose new ideas and connect with contributors.
