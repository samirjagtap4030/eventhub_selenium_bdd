// ============================================================
// EventHub Selenium BDD — Jenkins Declarative Pipeline
//
// Runs the Cucumber suite in 4 parallel parts (one scenario each).
// Each part runs in its own agent workspace to avoid file conflicts.
// Screenshots are captured on failure by CucumberHooks and saved
// under target/screenshots/ for Jenkins artifact archiving.
// A merged Cucumber HTML report is published at the end.
// ============================================================

pipeline {

    // No top-level agent — each parallel stage allocates its own node
    // so every branch gets an isolated workspace and Maven target/ dir.
    agent none

    options {
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
    }

    environment {
        MAVEN_OPTS = '-Xmx512m -Xms256m'
        BROWSER    = 'chrome'
    }

    // ──────────────────────────────────────────────────────────
    // STAGES
    // ──────────────────────────────────────────────────────────
    stages {

        // ----------------------------------------------------------
        // Stage 1 — Run all 4 parts simultaneously
        // ----------------------------------------------------------
        stage('Run Tests — Parallel') {
            parallel {

                // ── Part 1: Smoke / booking list ──────────────────
                stage('Part 1 — Booking List') {
                    agent any
                    steps {
                        checkout scm
                        dir('selenium-tests') {
                            sh '''
                                mvn test \
                                  -Dsurefire.suiteXmlFiles=testng-bdd.xml \
                                  -Dcucumber.filter.tags="@part1" \
                                  -Dbrowser=${BROWSER} \
                                  ${MAVEN_OPTS:+-Xmx512m} \
                                  --no-transfer-progress
                            '''
                        }
                    }
                    post {
                        always {
                            dir('selenium-tests') {
                                // Archive screenshots captured on failure
                                archiveArtifacts(
                                    artifacts: 'target/screenshots/**/*.png',
                                    allowEmptyArchive: true
                                )
                                // Stash Cucumber JSON for report merging
                                stash(
                                    name: 'cucumber-part1',
                                    includes: 'target/cucumber-reports/cucumber.json',
                                    allowEmpty: true
                                )
                            }
                        }
                    }
                }

                // ── Part 2: Event & customer info ─────────────────
                stage('Part 2 — Event & Customer Info') {
                    agent any
                    steps {
                        checkout scm
                        dir('selenium-tests') {
                            sh '''
                                mvn test \
                                  -Dsurefire.suiteXmlFiles=testng-bdd.xml \
                                  -Dcucumber.filter.tags="@part2" \
                                  -Dbrowser=${BROWSER} \
                                  --no-transfer-progress
                            '''
                        }
                    }
                    post {
                        always {
                            dir('selenium-tests') {
                                archiveArtifacts(
                                    artifacts: 'target/screenshots/**/*.png',
                                    allowEmptyArchive: true
                                )
                                stash(
                                    name: 'cucumber-part2',
                                    includes: 'target/cucumber-reports/cucumber.json',
                                    allowEmpty: true
                                )
                            }
                        }
                    }
                }

                // ── Part 3: Payment info ───────────────────────────
                stage('Part 3 — Payment Info') {
                    agent any
                    steps {
                        checkout scm
                        dir('selenium-tests') {
                            sh '''
                                mvn test \
                                  -Dsurefire.suiteXmlFiles=testng-bdd.xml \
                                  -Dcucumber.filter.tags="@part3" \
                                  -Dbrowser=${BROWSER} \
                                  --no-transfer-progress
                            '''
                        }
                    }
                    post {
                        always {
                            dir('selenium-tests') {
                                archiveArtifacts(
                                    artifacts: 'target/screenshots/**/*.png',
                                    allowEmptyArchive: true
                                )
                                stash(
                                    name: 'cucumber-part3',
                                    includes: 'target/cucumber-reports/cucumber.json',
                                    allowEmpty: true
                                )
                            }
                        }
                    }
                }

                // ── Part 4: Cancellation ───────────────────────────
                stage('Part 4 — Cancellation') {
                    agent any
                    steps {
                        checkout scm
                        dir('selenium-tests') {
                            sh '''
                                mvn test \
                                  -Dsurefire.suiteXmlFiles=testng-bdd.xml \
                                  -Dcucumber.filter.tags="@part4" \
                                  -Dbrowser=${BROWSER} \
                                  --no-transfer-progress
                            '''
                        }
                    }
                    post {
                        always {
                            dir('selenium-tests') {
                                archiveArtifacts(
                                    artifacts: 'target/screenshots/**/*.png',
                                    allowEmptyArchive: true
                                )
                                stash(
                                    name: 'cucumber-part4',
                                    includes: 'target/cucumber-reports/cucumber.json',
                                    allowEmpty: true
                                )
                            }
                        }
                    }
                }

            } // end parallel
        } // end stage Run Tests — Parallel

        // ----------------------------------------------------------
        // Stage 2 — Merge JSON files and publish unified HTML report
        // ----------------------------------------------------------
        stage('Publish Cucumber Report') {
            agent any

            steps {
                // Collect all stashed Cucumber JSON results
                unstash 'cucumber-part1'
                unstash 'cucumber-part2'
                unstash 'cucumber-part3'
                unstash 'cucumber-part4'

                // Merge into a single directory that the plugin reads
                sh '''
                    mkdir -p cucumber-merged
                    for part in 1 2 3 4; do
                        src="selenium-tests/target/cucumber-reports/cucumber.json"
                        dest="cucumber-merged/cucumber-part${part}.json"
                        [ -f "$src" ] && cp "$src" "$dest" || echo "No JSON for part ${part}"
                    done
                '''

                // Publish Cucumber HTML report
                // Requires the "Cucumber Reports" Jenkins plugin
                // (manage-plugins → cucumber-reporting)
                cucumber(
                    fileIncludePattern: 'cucumber-merged/*.json',
                    jsonReportDirectory: 'cucumber-merged',
                    reportTitle: 'EventHub BDD — Cucumber Report',
                    buildStatus: 'UNSTABLE',
                    classifications: [
                        [key: 'Browser', value: "${env.BROWSER}"],
                        [key: 'Build',   value: "${env.BUILD_NUMBER}"]
                    ]
                )
            }

            post {
                always {
                    // Also archive the raw JSON so teams can import it elsewhere
                    archiveArtifacts(
                        artifacts: 'cucumber-merged/*.json',
                        allowEmptyArchive: true
                    )
                }
            }
        }

    } // end stages

    // ──────────────────────────────────────────────────────────
    // GLOBAL POST
    // ──────────────────────────────────────────────────────────
    post {
        failure {
            echo "Pipeline FAILED — check archived screenshots and the Cucumber report for details."
        }
        unstable {
            echo "Pipeline UNSTABLE — one or more Cucumber scenarios failed."
        }
        success {
            echo "All 4 parts passed. Cucumber report published."
        }
    }

}
