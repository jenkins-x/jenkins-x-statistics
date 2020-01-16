@Grapes([
	@Grab('org.yaml:snakeyaml:1.17'),
	@Grab('commons-beanutils:commons-beanutils:1.9.3'),
	@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
])

import groovy.time.*
import groovy.json.*
import org.yaml.snakeyaml.Yaml
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.URLENC
import groovyx.net.http.ContentType
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

String.metaClass.json = {
	new JsonSlurper().parseText(delegate)
}

String.metaClass.encode = { 
	java.net.URLEncoder.encode(delegate, "UTF-8")
}

String.metaClass.toDate {
	Date.parse("yyyy-MM-dd'T'HH:mm", delegate)
}

Yaml parser = new Yaml()

def home = new File(System.getenv('HOME'))
def git = parser.load(new File(home,".config/hub").text)
def gitAuth = git.'github.com'.get(0)
def baseUrl = "https://${gitAuth.user}:${gitAuth.oauth_token}@api.github.com"

def year = '2019'
def orgs = ['jenkins-x','jenkins-x-apps','jenkins-x-charts','jenkins-x-buildpacks','jenkins-x-quickstarts','jenkins-x-images']
def repos = []
orgs.each { org ->
	def page = 1
	def per_page = 100
	def more = true

	println org

	while(more) {
		println "Getting page ${page} for ${org}"
		def reposList = "curl ${baseUrl}/orgs/${org}/repos?per_page=${per_page}&page=${page}".execute().text.json()
	
		page++

		if (reposList.size() < per_page) {
			more = false
		}
	
		reposList.each{ 
			repos << [name: it.name, org: org, archived: it.archived, disabled: it.disabled]
		}
	}
}

println repos.size()
println repos.findAll{ !it.archived }.findAll{ !it.disabled }.size()

def issues = []
def commits = []

repos.findAll{ !it.archived }.findAll{ !it.disabled }.each { repo ->
	def more = true
	def page = 1
	def per_page = 100
	
	while (more) {
		println "Getting page ${page} of issues on ${repo.org}/${repo.name}"
		def ghIssues = "curl ${baseUrl}/repos/${repo.org}/${repo.name}/issues?state=all&per_page=${per_page}&page=${page}".execute().text.json()

		ghIssues.each{
        	def createdAt = it.created_at.toDate()
			def pr = false
			if (it.pull_request) {
				pr = true
			}
			def issue = [repo: repo, number: it.number, createdAt: createdAt, author: it.author_association, pr: pr, user: it.user.login]
			issues << issue
		}

		page++

		if (ghIssues.size() < per_page) {
			more = false
		}
	}

	more = true
	page = 1

	while (more) {
		// /repos/:owner/:repo/commits
		println "Getting page ${page} of commits on ${repo.org}/${repo.name}"
		def ghCommits = "curl ${baseUrl}/repos/${repo.org}/${repo.name}/commits?since=${year}-01-01T00:00:00Z&until=${year}-12-31T23:59:59Z&per_page=${per_page}&page=${page}".execute().text.json()

		def message = ghCommits.message
		if (message == 'Git Repository is empty.') {
			println ghCommits.message
		} else {
			ghCommits.each{
        		def createdAt = it.commit.author.date.toDate()
				def commit = [repo: repo, sha: it.sha, createdAt: createdAt, user: it.commit.author.name]
				commits << commit
			}
		}

		page++

		if (ghCommits.size() < per_page) {
			more = false
		}
	}

}

def issuesByYear = issues.findAll{ it.createdAt.format('yyyy') == year }

def forYear = issuesByYear.findAll{ it.pr }.size()
def community = issuesByYear.findAll{ it.pr }.findAll{ it.author == 'NONE' }.size()
def member = issuesByYear.findAll{ it.pr }.findAll{ it.author == 'MEMBER' }.size()
def contributor = issuesByYear.findAll{ it.pr }.findAll{ it.author == 'CONTRIBUTOR' }.size()

println "For ${year}    : ${forYear}"
println "PR Break Down"
println "-----------------"
println "NONE        : ${community}"
println "MEMBER      : ${member}"
println "CONTRIBUTOR : ${contributor}"

def prContrib = issuesByYear.findAll{ it.pr }.collect{ it.user }.unique().size()
def issueContrib = issuesByYear.collect{ it.user }.unique().size()

println "-----------------"
println "PR Contrib    : ${prContrib}"
println "Issue Contrib : ${issueContrib}"

def automatedPrs = issuesByYear.findAll{ it.pr }.findAll{ it.user.contains('-bot') }.size()
def userPrs = issuesByYear.findAll{ it.pr }.findAll{ !it.user.contains('-bot') }.size()

println "-----------------"
println "automated     : ${automatedPrs}"
println "user          : ${userPrs}"

def numCommits = commits.size()
def numAutomatedCommits = commits.findAll{ it.user.contains('-bot') }.size()
def numUserCommits = commits.findAll{ !it.user.contains('-bot') }.size()

println "-----------------"
println "commits       : ${numCommits}"
println "automated     : ${numAutomatedCommits}"
println "user          : ${numUserCommits}"
