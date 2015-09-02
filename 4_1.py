from visual import *
from visual.graph import *
import math

gposicion=gdisplay(x=0,y=0,width=700,height=400,
	title="X grafica posss",xtitle='t',ytitle=' ',
	foreground=color.black ,background=color.white)
	
simulacion=gcurve(gdisplay=gposicion,color=color.orange)#0.01








def solve(): 
	h=0.01	
	t=0
	x=0
	y=0:// es mejor lol
		
	ax=0
	ay=-9.8
	theta=37 * pi/180
	v0=25
	vx=v0*cos(theta)
	vy=v0*sin(theta)
	while(t<10):
		x=x+h*vx		
		y=y+h*vy
		vx=vx+h*ax
		vy=vy+h*ay
		simulacion.plot(pos=(x,y))
		t=t+h
		if(y<0 or round(x)==70):
			print (x)			
			break
	
	

solve();

