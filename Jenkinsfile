// ============================================================
// EventHub Selenium BDD — Jenkins Declarative Pipeline (Windows)
//
// Runs the Cucumber suite in 4 parallel parts (one scenario each).
// Each part runs in its own agent workspace to avoid file conflicts.
// Screenshots are captured on failure by CucumberHooks and saved
// under target/screenshots/ for Jenkins artifact archiving.
// Cucumber HTML + JSON reports are archived at the end.
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
        BROWSER = 'chrome'
    }

    // ──────────────────────────────────────────────────────────
    // STAGES
    // ──────────────────────────────────────────────────────────
    stages {

        // ----------------------------------------------------------
        // Stage 1 — Run all 4 parts simultaneously
        // failFast: false ensures all parts run even if one fails.
        // catchError in each step converts FAILURE→UNSTABLE so the
        // Publish stage is not skipped when scenarios fail.
        // ----------------------------------------------------------
        stage('Run Tests — Parallel') {
            parallel {

                // ── Part 1: Smoke / booking list ──────────────────
                stage('Part 1 — Booking List') {
                    agent any
                    steps {
                        checkout scm
                        dir('selenium-tests') {
                            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                bat "mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags=@part1 -Dbrowser=${env.BROWSER} --no-transfer-progress"
                            }
                        }
                    }
                    post {
                        always {
                            dir('selenium-tests') {
                                // Archive failure screenshots
                                archiveArtifacts(
                                    artifacts: 'target/screenshots/**/*.png',
                                    allowEmptyArchive: true
                                )
                                // Stash Cucumber HTML + JSON for the publish stage
                                stash(
                                    name: 'cucumber-part1',
                                    includes: 'target/cucumber-reports/**',
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
                            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                bat "mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags=@part2 -Dbrowser=${env.BROWSER} --no-transfer-progress"
                            }
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
                                    includes: 'target/cucumber-reports/**',
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
                            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                bat "mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags=@part3 -Dbrowser=${env.BROWSER} --no-transfer-progress"
                            }
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
                                    includes: 'target/cucumber-reports/**',
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
                            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                bat "mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags=@part4 -Dbrowser=${env.BROWSER} --no-transfer-progress"
                            }
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
                                    includes: 'target/cucumber-reports/**',
                                    allowEmpty: true
                                )
                            }
                        }
                    }
                }

            } // end parallel
        } // end stage Run Tests — Parallel

        // ----------------------------------------------------------
        // Stage 2 — Collect reports from all 4 workspaces and archive
        // This stage runs even when scenarios fail (UNSTABLE state).
        // ----------------------------------------------------------
        stage('Publish Cucumber Report') {
            agent any

            steps {
                // Each unstash restores target/cucumber-reports/ from its
                // parallel workspace. We archive immediately after each
                // unstash so reports are not overwritten by the next one.
                unstash 'cucumber-part1'
                archiveArtifacts(
                    artifacts: 'selenium-tests/target/cucumber-reports/**',
                    allowEmptyArchive: true
                )

                unstash 'cucumber-part2'
                archiveArtifacts(
                    artifacts: 'selenium-tests/target/cucumber-reports/**',
                    allowEmptyArchive: true
                )

                unstash 'cucumber-part3'
                archiveArtifacts(
                    artifacts: 'selenium-tests/target/cucumber-reports/**',
                    allowEmptyArchive: true
                )

                unstash 'cucumber-part4'
                archiveArtifacts(
                    artifacts: 'selenium-tests/target/cucumber-reports/**',
                    allowEmptyArchive: true
                )

                // To get a rendered Cucumber UI graph in Jenkins, install the
                // "Cucumber Reports" plugin and replace archiveArtifacts with:
                //   cucumber fileIncludePattern: 'selenium-tests/target/cucumber-reports/cucumber.json',
                //            reportTitle: 'EventHub BDD Report',
                //            buildStatus: 'UNSTABLE'
            }
        }

    } // end stages

    // ──────────────────────────────────────────────────────────
    // GLOBAL POST
    // ──────────────────────────────────────────────────────────
    post {
        failure {
            echo "Pipeline FAILED — pipeline error (not test failure). Check the logs."
        }
        unstable {
            echo "Pipeline UNSTABLE — one or more Cucumber scenarios failed. Check archived screenshots and reports."
        }
        success {
            echo "All 4 parts PASSED. Cucumber reports archived."
        }
    }

}
