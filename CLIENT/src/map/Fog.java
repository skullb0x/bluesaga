package map;

import java.util.Iterator;
import java.util.Vector;

import org.newdawn.slick.Color;


public class Fog {
	private Vector<FogCloud> FogClouds;
	
	public Fog(int cameraStartX, int cameraStartY){
		FogClouds = new Vector<FogCloud>();
		for(int i = 1; i < 5; i++){
			FogCloud f = new FogCloud(i, cameraStartX,cameraStartY);
			FogClouds.add(f);
		}
	}
	
	public void draw(int cameraX, int cameraY, Color aColor){
		
		Iterator<FogCloud> cloudIterator = FogClouds.iterator();
		
		while(cloudIterator.hasNext()){
			FogCloud cloud = cloudIterator.next();
			cloud.draw(cameraX,cameraY,aColor);
			if(cloud.opacity == 0){
				cloudIterator.remove();
			}
		}
	}
	
	public void appear(int cameraStartX, int cameraStartY){
		int nrClouds = 0;
		for(FogCloud f: FogClouds){
			f.appear();
			nrClouds++;
		}
		
		for(int i = nrClouds+1; i < 5; i++){
			FogCloud f = new FogCloud(i, cameraStartX,cameraStartY);
			FogClouds.add(f);
		}
	}
	
	public void dissappear(){
		Iterator<FogCloud> cloudIterator = FogClouds.iterator();
		
		while(cloudIterator.hasNext()){
			FogCloud cloud = cloudIterator.next();
			cloud.dissappear();
		}
	}
}
