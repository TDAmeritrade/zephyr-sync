setup = [
	// Defines the Dockerfile to be used in the build.
	platforms: "zuluopenjdk:11.0.2, maven:3.6.1",
	//SCM System for application EX: CTG
	system: "BOS",
	//SCM Subsystem(s) for application EX: CTG_Accounts_Api
	subsystem: ["zephyr-sync"],
	//List of binary paths: jars, wars, etc
	binaries_path: ["zephyr-sync-cli/target/*.jar", "zephyr-sync-core/target/*.jar", "zephyr-sync-maven-plugin/target/*.jar", "zephyr-sync-report-allure/target/*.jar", "zephyr-sync-report-api/target/*.jar", "zephyr-sync-report-cucumber/target/*.jar", "zephyr-sync-report-junit/target/*.jar", "zephyr-sync-report-nunit/target/*.jar", "zephyr-sync-util/target/*.jar", "zephyr-sync-config/target/*.jar"],
	//Path to Parent Pom
	buildFile: "pom.xml",
	//Type of build and which type of auto-versioning occurs. EX: mvn, npm
	build_type: "maven",
	//Platform your app is hosted: SCM (TC Server), PCF
    runTypes: ["scm"],
	//Auto-versioning enabled
	autoVersioning: true,
	//Environments to Auto-deploy
	scm_auto_deploy_env: "DEV1",
	//Auto-Deploy Boolean Flag
	scm_auto_deploy_flag: "false",
	//Artifactory Snapshot Repository for temporary snapshots
	snapshotRepo: "bos-snapshots-local",
	//Artifactory Releases Repository for permanent versioned artifacts
	releaseRepo: "bos-releases-local",
	//Branch for BRE support scripts. Default: master
	ctgDeliveryToolsBranch: "master",
	//Team's Slack Channel For Build Status
	slack: [channel: 'corporate-actions-notifications']
]

//Default method from SDP Jenkins; Required to setup Docker and env variables
sdp(setup){
	println("-----Load BRE Library-----")
    library identifier: 'ctg-delivery-tools@'+setup['ctgDeliveryToolsBranch'], retriever: modernSCM([$class: 'GitSCMSource', credentialsId: 'zjenkins', id: '6286e4ae-b441-41df-b5d8-caa87f4f0ec2', remote: 'https://bitbucket.associatesys.local/scm/bosc/ctg-delivery-tools.git', traits: [[$class: 'jenkins.plugins.git.traits.BranchDiscoveryTrait']]])
    
    //Setup Environment for BRE scripts and CTG execution. Includes auto-versioning and commit auto-deploy
    ctg_pipeline_env_setup setup
    ctg_prebuild_setup setup 

	// //Documentation of GPL Functions https://confluence.associatesys.local/display/SDP/SDP+GPL+Functions
	build.run type:'maven', 
		buildFile: setup['buildFile'],
		buildGoals: 'clean '+setup['codeCoveragePlugin']+' install -ntp', 
		buildInfo: true, 
		snapshotRepo: setup['snapshotRepo'], 
		releaseRepo: setup['releaseRepo'],
		securitySuiteBranchPattern: "develop", 
		junitTestResults: setup['junitTestResultsLocation'], 
		sonarAddArgs: "-q"

	//Loop through subsystems and binaries_path to pack and upload to Artifactory respectively. Required for deployment
	setup['subsystem'].eachWithIndex { subsystem, index ->
		pack.run type:'scm', 
			system: setup['system'], 
			subsystem: subsystem, 
			binary: setup['binaries_path'][index], 
			version: setup['version']
	}

	//BRE Post-build Actions
    ctg_postbuild(setup)
}