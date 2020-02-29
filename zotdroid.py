#!/usr/bin/python3

import cherrypy

from os import listdir
from os.path import isfile, join

class ZotDroid:
	@cherrypy.expose
	def index(self, *args, **kwargs):
		r = cherrypy.response
		r.headers['Content-Type'] = 'text/plain'
		content = "Positional arguments\n\n"
		for k in args:
		  content += k + "\n"
		content += "\nKeyword arguments\n\n"
		for k in kwargs:
		  content += k + ": " + kwargs[k] + "\n"
		
		onlyfiles = [f for f in listdir(".") if isfile(join(".", f))]
		fn = "report" + str(len(onlyfiles)) + ".txt"
		with open(fn,"w") as f:
			f.write(content)
		
		return content
		
	index.exposed = True
	
	
def application(environ, start_response):
	cherrypy.tree.mount(ZotDroid(), '/', None)
	return cherrypy.tree(environ, start_response)
	
if __name__ == "__main__":
	cherrypy.quickstart(ZotDroid())