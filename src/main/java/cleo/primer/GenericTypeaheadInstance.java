/*
 * Copyright (c) 2011-2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package cleo.primer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cleo.primer.rest.model.ElementDTO;
import cleo.search.Element;
import cleo.search.Indexer;
import cleo.search.MultiIndexer;
import cleo.search.selector.ScoredElementSelectorFactory;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.MultiArrayStoreElement;
import cleo.search.tool.GenericTypeaheadInitializer;
import cleo.search.typeahead.GenericTypeahead;
import cleo.search.typeahead.GenericTypeaheadConfig;
import cleo.search.typeahead.MultiTypeahead;
import cleo.search.typeahead.Typeahead;
import cleo.search.typeahead.TypeaheadConfigFactory;

/**
 * GenericTypeaheadInstance
 * 
 * @author jwu
 * @since 12/22, 2011
 */
public class GenericTypeaheadInstance<E extends Element> implements TypeaheadInstance<E> {
    private Indexer<E> indexer;
    private  Typeahead<E> searcher;
    private ArrayStoreElement<E> elementStore;
    final Map<String,GenericTypeahead<E>> federatedIndexer;
    final Map<String,Integer> indexLength;
    final String name;
    List<Indexer<E>> indexerList = new ArrayList<Indexer<E>>();
    List<Typeahead<E>> searcherList = new ArrayList<Typeahead<E>>();
    List<ArrayStoreElement<E>> storeList = new ArrayList<ArrayStoreElement<E>>();
    private File templateConfig;
    private File dataFolder;
    GroupNamePersistance groupNamePersistance;
    
    public GenericTypeaheadInstance(String defaultGroupName , File templateConfig , File dataFolder, GroupNamePersistance groupNamePersistance) throws Exception {
        this.name = defaultGroupName;
        this.templateConfig = templateConfig;
        this.dataFolder = dataFolder;
        federatedIndexer = new HashMap<String,GenericTypeahead<E>>();
        indexLength = new HashMap<String,Integer>();
        this.groupNamePersistance = groupNamePersistance;
        initializeConnections(dataFolder,new ArrayList<String>(groupNamePersistance.read().keySet()));
        createNewGroup(name);
    }
    
    
    public void createNewGroup(String name) throws Exception{
    	if(federatedIndexer.get(name) != null){
    		return;
    	}
    	GenericTypeahead<E> gta = createTypeahead(templateConfig,name);
    	federatedIndexer.put(name, gta);
        indexerList.add(gta);
        searcherList.add(gta);
        storeList.add(gta.getElementStore());
        indexer = new MultiIndexer<E>(this.name, indexerList);
        searcher = new MultiTypeahead<E>(this.name, searcherList);
        elementStore = new MultiArrayStoreElement<E>(storeList);
        elementStore.persist();
        groupNamePersistance.addGroupName(name);
    }
    
    protected GenericTypeahead<E> createTypeahead(File configFile , String name) throws Exception {
        GenericTypeaheadConfig<E> config = TypeaheadConfigFactory.createGenericTypeaheadConfig(configFile);
        config.setSelectorFactory(new ScoredElementSelectorFactory<E>());
        
        config.setName(name);
        config.setConnectionsStoreDir(appendName(dataFolder,name + "/connections-store"));
        config.setElementStoreDir(appendName(dataFolder,name + "/element-store"));
        System.out.println("Config is " + config.getConnectionsStoreDir() + " , " + config.getElementStoreDir());
        indexLength.put(name, 0);
        GenericTypeaheadInitializer<E> initializer =new GenericTypeaheadInitializer<E>(config);
        
        return (GenericTypeahead<E>)initializer.getTypeahead();
    }
    
    private File appendName(File file , String name){
    	if(file == null){
    		return null;
    	}
    	File ret = new File(file.getAbsoluteFile() + "/" + name);
    	System.out.println("File to be loaded is " + ret);
    	return ret;
    }
    
    private List<GenericTypeahead<E>> initializeConnections(File dataFolder, List<String> list) throws Exception{
    	List<GenericTypeahead<E>> connections = new ArrayList<GenericTypeahead<E>>();
    	System.out.println("List of sources are " + list);
    	for(String groupName : list ){
    	        GenericTypeahead<E> gta = createTypeahead(templateConfig,groupName);
    			connections.add(gta);
    	        indexerList.add(gta);
    	        searcherList.add(gta);
    	        storeList.add(gta.getElementStore());
    	        indexLength.put(groupName, getLargest(gta.getElementStore()));
    	        federatedIndexer.put(groupName, gta);
    			System.out.println("Adding new connection " + groupName + "with size " + indexLength.get(groupName));
    	}

        indexer = new MultiIndexer<E>(this.name, indexerList);
        searcher = new MultiTypeahead<E>(this.name, searcherList);
        elementStore = new MultiArrayStoreElement<E>(storeList);
//        indexer.flush();
//        elementStore.persist();
    	return connections;
    }
    public int getLargest(ArrayStoreElement<E> store) {
        int largest=0;
        int start = store.getIndexStart();
        int end = start + store.length();
        for(int i = start; i < end; i++) {
            E element = store.getElement(i);
            if(element != null && ((ElementDTO) element).isSearchable()) {
                if(largest < i){
                        largest=i;
                }
            }
        }
        return largest;
    }
    
    public final Boolean index(E element , String name) throws Exception {
    	GenericTypeahead<E> index = federatedIndexer.get(name);
    	if(index == null){
    		createNewGroup(name);
    		index = federatedIndexer.get(name);
    	}
    	if(name == null || name.isEmpty()){
    		name = this.name;
    	}
    	Integer length = indexLength.get(name);
    	element.setElementId(length + 1);
    	Boolean flag = index.index(element);
    	if(flag == true){
    		indexLength.put(name, length + 1);
    	}
    	return flag;
    }
    
    public final Boolean index(E element) throws Exception {
    	return index(element , name);
    }
    
    public final Typeahead<E> getSearcher() {
        return searcher;
    }
    
    public final ArrayStoreElement<E> getElementStore() {
        return elementStore;
    }

	public Indexer<E> getIndexer() {
		return indexer;
	}
}
