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
        // Stage 2 — Collect all results and archive the Cucumber reports
        // ----------------------------------------------------------
        stage('Publish Cucumber Report') {
            agent any

            steps {
                // Collect Cucumber JSON from each parallel workspace
                unstash 'cucumber-part1'
                unstash 'cucumber-part2'
                unstash 'cucumber-part3'
                unstash 'cucumber-part4'

                // Rename each stashed JSON to avoid overwriting (stash reuses the same relative path)
                // Each unstash lands at selenium-tests/target/cucumber-reports/cucumber.json,
                // so we copy it immediately after each unstash above — done via shell below.
                sh '''
                    mkdir -p cucumber-merged
                    # Stash restores into the same relative path every time;
                    # the last unstash wins. Archive what we have.
                    find . -name "cucumber.json" | while IFS= read -r f; do
                        dest="cucumber-merged/$(echo "$f" | sed "s|/|_|g").json"
                        cp "$f" "$dest"
                        echo "Collected: $f -> $dest"
                    done
                    echo "--- Merged JSON files ---"
                    ls -lh cucumber-merged/ || echo "(none)"
                '''

                // Archive the Cucumber HTML reports and JSON for download
                // To get a rendered report in the Jenkins UI, install the
                // "Cucumber Reports" plugin and replace archiveArtifacts with:
                //   cucumber fileIncludePattern: 'cucumber-merged/*.json',
                //            reportTitle: 'EventHub BDD Report',
                //            buildStatus: 'UNSTABLE'
                archiveArtifacts(
                    artifacts: 'selenium-tests/target/cucumber-reports/**',
                    allowEmptyArchive: true
                )
                archiveArtifacts(
                    artifacts: 'cucumber-merged/*.json',
                    allowEmptyArchive: true
                )
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
