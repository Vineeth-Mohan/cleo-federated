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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cleo.primer.rest.model.ElementDTO;
import cleo.search.Indexer;
import cleo.search.store.ArrayStoreElement;
import cleo.search.typeahead.Typeahead;

/**
 * ElementDAO - A singleton Data Access Object (DAO) for elements of type {@link ElementDTO}. 
 * 
 * @author jwu
 * @since 12/22, 2012
 */
public enum ElementDAO implements RestDAO<ElementDTO> {
    INSTANCE;
    
    private GenericTypeaheadInstance<ElementDTO> loader;
    
    private ElementDAO () {
        try {
        	Properties prop = new Properties();
        	prop.load(new FileInputStream("config/cleo.properties"));
            String defaultGroupName = prop.getProperty("defaultGroupName");
            String templateConfig = prop.getProperty("templateConfig");
            String dataDirectory = prop.getProperty("dataDirectory");
            System.out.println("Config are " + defaultGroupName + " , " + templateConfig + " , " + dataDirectory);
            File confFile = new File(templateConfig);
            File basePath = new File(dataDirectory);
            if(!basePath.exists()){
            	basePath.mkdirs();
            }
            loader = new GenericTypeaheadInstance<ElementDTO>(defaultGroupName,confFile,basePath,new GroupNamePersistance(dataDirectory));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public final Indexer<ElementDTO> getIndexer() {
        return loader.getIndexer();
    }
    
    public final Typeahead<ElementDTO> getSearcher() {
        return loader.getSearcher();
    }
    
    public final ArrayStoreElement<ElementDTO> getElementStore() {
        return loader.getElementStore();
    }
    
    public List<ElementDTO> getElements() {
        ArrayStoreElement<ElementDTO> store = getElementStore();
        List<ElementDTO> list = new ArrayList<ElementDTO>(store.capacity());
        
        int start = store.getIndexStart();
        int end = start + store.length();
        for(int i = start; i < end; i++) {
            ElementDTO element = getElementStore().getElement(i);
            if(element != null && element.isSearchable()) {
                list.add(element);
            }
        }
        
        return list;
    }
    
    @Override // HTTP-GET
    public ElementDTO getElement(int elementId) {
        ElementDTO element = getElementStore().getElement(elementId);
        if(element != null && element.isSearchable()) {
            return element;
        } else {
            return null;
        }
    }
    
    @Override // HTTP-DELETE
    public ElementDTO deleteElement(int elementId) throws Exception {
        ElementDTO oldElement = getElementStore().getElement(elementId);
        
        if(oldElement != null) {
            ElementDTO newElement = (ElementDTO)oldElement.clone();
            newElement.setTerms(new String[0]);
            getIndexer().index(newElement);
        }
        
        if(oldElement != null && oldElement.isSearchable()) {
            return oldElement;
        } else {
            return null;
        }
    }
    
    @Override // HTTP-PUT
    public ElementDTO updateElement(ElementDTO element) throws Exception {
        ElementDTO oldElement = getElementStore().getElement(element.getElementId());
        getIndexer().index(element);
        
        if(oldElement != null && oldElement.isSearchable()) {
            return oldElement;
        } else {
            return null;
        }
    }
    
    public boolean insertElement(ElementDTO element) throws Exception {
        return loader.index(element);
    }
    
    @Override // HTTP-POST
    public boolean insertElementOfType(String name , ElementDTO element) throws Exception {
        return loader.index(element,name);
    }

}
