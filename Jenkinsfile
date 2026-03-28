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
        // ----------------------------------------------------------
        stage('Run Tests — Parallel') {
            parallel {

                // ── Part 1: Smoke / booking list ──────────────────
                stage('Part 1 — Booking List') {
                    agent any
                    steps {
                        checkout scm
                        dir('selenium-tests') {
                            bat "mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags=@part1 -Dbrowser=${env.BROWSER} --no-transfer-progress"
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
                            bat "mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags=@part2 -Dbrowser=${env.BROWSER} --no-transfer-progress"
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
                            bat "mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags=@part3 -Dbrowser=${env.BROWSER} --no-transfer-progress"
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
                            bat "mvn test -Dsurefire.suiteXmlFiles=testng-bdd.xml -Dcucumber.filter.tags=@part4 -Dbrowser=${env.BROWSER} --no-transfer-progress"
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
        // Stage 2 — Collect results and archive the Cucumber reports
        // ----------------------------------------------------------
        stage('Publish Cucumber Report') {
            agent any

            steps {
                // Unstash reports from all 4 parallel workspaces.
                // Each lands at selenium-tests/target/cucumber-reports/;
                // the last one wins, so we archive after every unstash.
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

                // NOTE: To get a rendered Cucumber UI report in Jenkins,
                // install the "Cucumber Reports" plugin and add:
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
            echo "Pipeline FAILED — check archived screenshots and the Cucumber report for details."
        }
        unstable {
            echo "Pipeline UNSTABLE — one or more Cucumber scenarios failed."
        }
        success {
            echo "All 4 parts passed. Cucumber reports archived."
        }
    }

}
