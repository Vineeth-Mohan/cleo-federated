package cleo.primer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class GroupNamePersistance {
	
	String dataDirectory;
	String file;
	Map<String,Double> groupNames;
	
	public GroupNamePersistance(String dataDirectory) throws Exception{
		this.dataDirectory = dataDirectory;
		file = dataDirectory + "/groups.list";
		groupNames = read();
	}
	
	public void addGroupName(String name) throws Exception{
		groupNames.put(name,0.0);
		persist();
	}
	
	public void updateGroup(String name,Double size) throws Exception{
		if(groupNames.keySet().contains(name)){
			groupNames.put(name,size);
		}
		persist();
	}

	
	public void persist() throws Exception{
	     FileOutputStream fo = new FileOutputStream(file);  
	     ObjectOutputStream oo=new ObjectOutputStream(fo);  
	     oo.writeObject(groupNames);  
	}
	
	public HashMap<String,Double> read() throws Exception{
		if(!(new File(file).exists())){
			return new HashMap<String,Double>();
		}
	    FileInputStream fi=new FileInputStream(file);  
	    ObjectInputStream oi=new ObjectInputStream(fi);  
	    HashMap<String,Double> groupNames=(HashMap<String,Double>) oi.readObject();  
		return groupNames;
	}

}
