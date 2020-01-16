# Project to provide a list of statistics covering Jenkins X

To run the scripts, you need to have groovy installed.

The scripts also assume that your github access token is stored in $HOME/.hub/config (https://github.com/github/hub)

## Number of Jenkins X releases
  
This will only look at releases in the jenkins-x/jx repository

```
groovy ./get_releases.groovy
```

## Number of Commits, Contributers & Pull Requests

This will look at all jenkins-x orgs, which currently are:

* jenkins-x
* jenkins-x-buildpacks
* jenkins-x-apps
* jenkins-x-charts
* jenkins-x-quickstarts
* jenkins-x-images

```
groovy ./get_all.groovy
```

NOTE: this second script will take about 20mins to complete

# The 2019 Stats

| Stat          | Value         |
| ------------- |--------------:|
| JX Cli Releases        | 1336 |
| Repos                  | 156 |
| Number of PRs          |
|  total                 | 16836 |
|  by members            | 15695 |
|  by contributers       | 699 |
|  by external           | 327 |
|  automated             | 11551 |
|  user                  | 5285 |
| Number of Contributers | 
|  PRs                   | 307 |
|  Issues                | 694 |
| Number of Commits      | 
|  total                 | 46590 |
|  automated             | 32182 |
|  user                  | 14408 |
