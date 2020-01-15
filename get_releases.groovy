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

def releases = []
def moreReleases = true
def page = 1
def per_page = 100

while (moreReleases) {
	println "Getting page ${page}"
	def ghIssues = "curl https://${gitAuth.user}:${gitAuth.oauth_token}@api.github.com/repos/jenkins-x/jx/releases?per_page=${per_page}&page=${page}".execute().text.json()

	ghIssues.each{
        def name = it.name
        def createdAt = it.created_at.toDate()
        //println it
		//def issueKind = it.labels.find{ label -> label.name.startsWith('kind')}?.name
		//def issuePriority = it.labels.find{ label -> label.name.startsWith('priority')}?.name
	//	def issueArea = it.labels.find{ label -> label.name.startsWith('area')}?.name
	//	def issueLifecycle = it.labels.find{ label -> label.name.startsWith('lifecycle')}?.name
		releases << [name: name, createdAt: createdAt]
	}

	page++

	if (ghIssues.size() < per_page) {
		moreReleases = false
	}
}

println "All Release(s) ${releases.size()}"

def forYear = releases.findAll{ it.createdAt.format('yyyy') == '2019' }.size()
println "For 2019 ${forYear}"

