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
