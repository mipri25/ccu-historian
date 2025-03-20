package mdz.ccuhistorian.webapp

import groovy.util.logging.Log
import java.util.prefs.Preferences
import mdz.Exceptions
import mdz.hc.itf.hm.HmSysVarInterface

@Log
public class PageRenderer {

    // the "user" of this class
    def servlet
    
    // closures with page content, delegate is set to servlet.html
    def head
    def content
    def end

    // render page content
    public render() {
		long st=System.currentTimeMillis()
        servlet.utils.catchToLog(log) {
            setup()
            handleUserLogInOut()
            writeDocument()
        }
		log.finest "Page build time: ${System.currentTimeMillis()-st} ms"
    }
    
	private def setup() {
        if (!servlet) 
            throw new IllegalStateException('Field servlet is not set')
		
        // setup session
        if (!servlet.session) {
            servlet.session=servlet.request.session
            servlet.session.maxInactiveInterval=1800
        }
        
        // setup request context
        servlet.ctx=servlet.session.getAttribute('ctx')
        if (!servlet.ctx) {
            servlet.ctx=[
                prefs: Preferences.userRoot().node('mdz/ccuhistorian/webpages'),
                user: [loggedIn:false, logInFailed:false]
            ]
            servlet.session.setAttribute('ctx', servlet.ctx)
        }
	}
	
	private def runSafe(cl) {
		if (cl!=null) {
			def e=servlet.utils.catchToLog(log) {
				cl.delegate=servlet.html
				cl()
			}
			if (e) {
				// show error description
				servlet.html.div(class:'alert alert-danger') {
					h4 'Fehler:'
					translateError(e)
					button class:'btn btn-default', type:'button', 'data-toggle':'collapse', 
						'data-target':'#errdescr', 'Details'
					div(class:'collapse', id:'errdescr') {
						pre Exceptions.getStackTrace(e)
					}
				}
			}
		}
	}
	
	private translateError(Exception e) {
		String msg=e.message?:e.class.name
		switch (msg) {
		case ~/(?s).*The write format 1 is smaller than the supported format 2.*/:
		    servlet.html.p {
				mkp.yield 'Die bestehende Datenbank muss für CCU-Historian Version 3 migriert werden! ' + 
					'Datenpunkte werden nicht weiter aufgezeichnet! Durch ein Zurückgehen auf ' +
					'die Version 2.9.0 kann die bestehende Datenbank unverändert weiter verwendet werden. '
				a href:'https://github.com/mdzio/ccu-historian/wiki/Migration-V3', 
					'Informationen zur Migration sind im Wiki zu finden.' 
			}
			return
		default:
			servlet.html.p msg
			return
		}
	}
	
	private def handleUserLogInOut() {
        // already logged in?
        if (!servlet.ctx.user.loggedIn) {
            def password_admin=servlet.ctx.prefs.get('password_admin', '')
            if (password_admin=='') {
                // auto login, if no password is set
                servlet.ctx.user.loggedIn=true
            } else if (servlet.params.login) {
                // check credentials
                if (password_admin==servlet.utils.secureHash(servlet.params.login_password)) {
                    servlet.ctx.user.loggedIn=true
                    servlet.ctx.user.logInFailed=false
                } else {
                    servlet.ctx.user.loggedIn=false
                    servlet.ctx.user.logInFailed=true
                }
            }
        } else {
            // logout?
            if (servlet.params.logout) {
                servlet.ctx.user.loggedIn=false
                servlet.ctx.user.logInFailed=false
            }
        }
	}
	
	private def writeDocument() {
		// start of HTML document
		servlet.println '<!doctype html>'
		servlet.html.html(lang:'de') {
			writeHead()
			writeBody()
		}
	}
	
	private def writeHead() {
		servlet.html.head {
			// standard headers
			meta charset:'utf-8'
			meta 'http-equiv':'X-UA-Compatible', content:'IE=edge'
			meta name:'viewport', content:'width=device-width, initial-scale=1'
			
			// bootstrap CSS
			link href:'/external/bootstrap/css/bootstrap.css', rel:'stylesheet'

			// historian CSS
			link href:'historian.css', rel:'stylesheet'
			
			// android icon
			link rel:'icon', sizes:'192x192', href:'historian-196.ico'
			// ios icon
			link rel:'apple-touch-icon', sizes:'180x180', href:'historian-180.png'
			
			// execute the head closure
			runSafe head
		}
	}
	
	private def writeBody() {
		servlet.html.body {
			div(class:'container-fluid', style:'margin-top: 0.5em') {
				if (!servlet.ctx.user.loggedIn) {
					writeLogIn()
				} else {
					writeNavigation()
					writeContent()
				}
			}
			
			// jquery JS
			script src:'/external/jquery/jquery.js'
			// bootstrap JS
			script src:'/external/bootstrap/js/bootstrap.js'
			// underscore JS
			script src:'/external/underscore/underscore.js'

			// execute the page end closure
			runSafe end
		}
	}
	
	private def writeLogIn() {
        // show a login dialog
		servlet.html.div(class:'row') {
			div(class:'col-md-4 col-md-offset-4') {
				div(class:'panel panel-default') {
					div(class:'panel-heading') {
						h3 class:'panel-title', 'CCU-Historian Anmeldung'
					}
					div(class:'panel-body') {
						if (servlet.ctx.user.logInFailed) {
							p class:'alert alert-danger', 'Die Anmeldung ist fehlgeschlagen!'
						}
						form(class:'form-horizontal', method:'post') {
							div(class:'form-group') {
								label class:'col-md-4 control-label', for:'input_password', 'Passwort:'
								div(class:'col-md-8') {
									input class:'form-control', type:'password', id:'input_password', name:'login_password', placeholder:'Passwort', autofocus:true
								}
							}
							div(class:'form-group') {
								div(class:'col-md-offset-4 col-md-8') {
									button class:'btn btn-default', type:'submit', name:'login', value:1, 'Anmelden'
								}
							}
						}
					}
				}
			}
		}
	}
	
	private def writeNavigation() {
		// navigation bar
		servlet.html.nav(class:'navbar navbar-default') {
			div(class:'container-fluid') {
				// header for mobile display
				div(class:'navbar-header') {
					button (type:'button', class:'navbar-toggle collapsed', 'data-toggle':'collapse', 'data-target':'#navbar-collapse-id') {
						span class:'icon-bar'
						span class:'icon-bar'
						span class:'icon-bar'
					}
					p class:'navbar-brand', 'CCU-Historian'
				}
				
				// navbar content
				div(class:'collapse navbar-collapse', id:'navbar-collapse-id') {
					ul(class:'nav navbar-nav') {
						// datapoint list
						li { a href:'/historian/index.gy', 'Datenpunktliste' }
						
						// tools
						li(class:'dropdown') {
							a(href:'#', class:'dropdown-toggle', 'data-toggle':'dropdown', role:'button') {
								mkp.yield 'Werkzeuge'
								span class:'caret'
							}
							ul(class:'dropdown-menu') {
								li { a href:'/historian/messages.gy', 'Meldungsanalyse' }
								li { a href:'/historian/dpconfig.gy', 'Datenpunktkonfiguration' }
								li { a href:'/historian/config.gy', 'Historian Konfiguration' }
								li { a href:'/historian/script.gy', 'Skriptumgebung' }
								// database web access
								def port
								servlet.utils.catchToLog(log) { 
									port=servlet.database.config.webPort 
								}
								if (port) {
									li { a href:"http://$servlet.webServer.historianAddress:$port", target:'_blank', 'Datenbank' }
								}
								li { a href:'/historian/expimp.gy', 'Datenbankexport/-import' }
								li { a href:'/historian/exprtrend.gy', 'Zeitreihenberechnung' }
							}
						}
						
						// CCU web UI
						// (each CCU has one HmSysVarInterface)
						def sysVarItfs=servlet.interfaceManager.interfaces.findAll { it.value instanceof HmSysVarInterface }
						if (sysVarItfs) {
							li(class:'dropdown') {
								a(href:'#', class:'dropdown-toggle', 'data-toggle':'dropdown', role:'button') {
									mkp.yield 'Zentralen'
									span class:'caret'
								}
								ul(class:'dropdown-menu') {
									def ccuNo=1
									sysVarItfs.each { itfName, itf ->
										li { a href:"http://$itf.scriptClient.address", target:'_blank', 'CCU '+(ccuNo>1?ccuNo:'') }
										ccuNo++
									}
								}
							}
						}
						
						// configured menu entries
						if (servlet.webServer.config.menuLinks) {
							li(class:'dropdown') {
								a(href:'#', class:'dropdown-toggle', 'data-toggle':'dropdown', role:'button') {
									mkp.yield 'Extras'
									span class:'caret'
								}
								ul(class:'dropdown-menu') {
									servlet.webServer.config.menuLinks.each { k, v ->
										li { a href:v.address, v.text }
									}
								}
							}
						}
					}
					
					// logout button
					if (servlet.ctx.prefs.get('password_admin', '')!='') {
						form(class:'navbar-form navbar-right') {
							div(class:'form-group') {
								button class:'btn btn-default', type:'submit', name:'logout', value:1, 'Abmelden'
							}
						}
					}
					
					// version
					p class:'navbar-text navbar-right', "V$servlet.utils.historianVersion"
				}
			}
		}
	}
	
	private def writeContent() {
        // execute the content closure
		runSafe content
	}
}
