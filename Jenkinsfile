#!groovy
def BN = BRANCH_NAME == "master" || BRANCH_NAME.startsWith("releases/") ? BRANCH_NAME : "master"

library "knime-pipeline@$BN"

properties([
	// provide a list of upstream jobs which should trigger a rebuild of this job
	pipelineTriggers([
		upstream('knime-core/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
		upstream('knime-shared/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
		upstream('knime-expressions/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
	]),
	buildDiscarder(logRotator(numToKeepStr: '5')),
	disableConcurrentBuilds()
])

try {
	// provide the name of the update site project
	knimetools.defaultTychoBuild('org.knime.update.base')

 	workflowTests.runTests(
        dependencies: [
            repositories: ['knime-base', 'knime-shared', 'knime-python',
            'knime-datageneration', 'knime-database', 'knime-timeseries',
            'knime-jep', 'knime-js-base'],
            // ius: ['org.knime.json.tests']
        ],
        withAssertions: true,
        // configurations: testConfigurations
    )

 	stage('Sonarqube analysis') {
 		env.lastStage = env.STAGE_NAME
 		workflowTests.runSonar()
 	}
 } catch (ex) {
	 currentBuild.result = 'FAILED'
	 throw ex
 } finally {
	 notifications.notifyBuild(currentBuild.result);
 }

/* vim: set ts=4: */
