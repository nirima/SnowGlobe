sinatra {

    def bastion;

    customer("allocate") {
        title = "Allocate Software"


        environment("support") {



            host("bastion") {
                ip = "192.168.1.26"


                services {

                    bastion = ssh {}

                }

            }
        }

        environment("dev") {



            host("jira") {

                ip = "sdc-supvjira02.allocatesoftware.com"

                services {
                    rdp {}
                }

            }

            host("wowbagger") {

                ip = "10.20.1.6"

                services {
                    rdp {}
                }
            }
        }
    }

    customer("hwph") {
        title = "Heatherwood & Wexham Park"

        environment("live") {

        }

        environment("qa") {

        }
    }

    customer("fph") {
        title = "Frimley Park"

         environment("live") {

         }

        environment("qa") {

        }
    }

    customer("ncuh") {
        title = "North Cumbria";

        environment("live") {
             host("appserver") {
                     ip = "10.129.172.37"


                     services {

                        ncuh_jump = ssh {
                            access_via = bastion
                        }

                        docker {
                            access_via = ncuh_jump
                        }

                        realtime {
                           access_via = ncuh_jump

                            port = 8080

                            credentials {
                                username = "realtime"
                                password = "r3aLt1mE"
                            }
                        }
                     }

                 }

                 host("database") {
                     ip = "10.129.149.88"
                     access_via = ncuh_jump

                     services {


                         sql {

                            credentials {
                                username = "rtsa"
                                password = "!!R4alT1me-"
                            }

                         }
                     }

                 }
            }

            environment("qa") {
                access_via = ncuh_jump
                host("appserver") {
                    ip = "10.129.149.178"


                    services {
                        ssh {

                        }

                        docker {

                        }

                         realtime { }
                    }

                }

                host("database") {
                ip = "10.129.149.88"

                services {

                    rdp {
                       credentials {
                        username = "realtime"
                        password = "r3aLt1mE"
                       }
                    }

                    sql {
                         access_via = ncuh_jump
                    }

                }
            }
        }


    }

    
}