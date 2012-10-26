package cleo.primer.rest.resources;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import cleo.primer.GenericTypeaheadInstance;
import cleo.primer.GroupNamePersistance;
import cleo.primer.rest.model.ElementDTO;
import cleo.search.ElementJavaSerializer;
import cleo.search.ElementSerializer;
import cleo.search.selector.ScoredPrefixSelectorFactory;
import cleo.search.selector.SelectorFactory;
import cleo.search.tool.ScannerTypeaheadInitializer;
import cleo.search.typeahead.ScannerTypeahead;
import cleo.search.typeahead.ScannerTypeaheadConfig;

public class TestCode  {
	
	public static void main(String[] args){
        try {
            ScannerTypeahead<ElementDTO> loader = createTypeahead();
            ElementDTO element = new ElementDTO();
            //element.setScore(0);
            element.setElementId(1);
            element.setName("president");
            element.setTerms("obama" , "barack");
            //element.setTimestamp(0);
            Boolean flag = loader.index(element);
            System.out.println("Elements are " + loader.search(0, new String[]{"o"}));
            System.out.println("Indexed return is " + flag);
        }catch(Exception e){
        	e.printStackTrace();
        }
        
	}

	
	  protected static ScannerTypeahead<ElementDTO> createTypeahead() throws Exception {
		    ScannerTypeaheadConfig<ElementDTO> config = new ScannerTypeaheadConfig<ElementDTO>();

		    String name = "vm";
		    config.setName(name);
		    config.setSelectorFactory(createSelectorFactory());
		    config.setElementSerializer(createElementSerializer());
		    config.setElementStoreDir(new File(name + "/element-store"));
		    config.setElementStoreIndexStart(0);
		    config.setElementStoreCapacity(100000);
		    config.setElementStoreSegmentMB(32);
		    config.setFilterPrefixLength(2);

		    ScannerTypeaheadInitializer<ElementDTO> initializer =
		      new ScannerTypeaheadInitializer<ElementDTO>(config);

		    return (ScannerTypeahead<ElementDTO>)initializer.getTypeahead();
		  }

	  protected static SelectorFactory<ElementDTO> createSelectorFactory() {
		    return new ScoredPrefixSelectorFactory<ElementDTO>();
		  }

	  protected static ElementSerializer<ElementDTO> createElementSerializer() {
		    return new ElementJavaSerializer<ElementDTO>();
		  }

}
