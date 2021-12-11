import static org.junit.jupiter.api.Assertions.*;

import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.Test;

import impl.SemanticCrawlerImpl;

class TestCrawler {

	@Test
	void test() {
		assertTrue(testURI("http://dbpedia.org/resource/Zico", "Zico"), "Zico");		
		//assertTrue(testURI("http://dbpedia.org/resource/Roger_Federer", "Roger"), "Roger Federer");
		
		assertTrue(getModel("http://dbpedia.org/resource/Zico").isIsomorphicWith(getModel("http://dbpedia.org/resource/Zico")));
	}
	
	boolean testURI(String resourceURI, String name)
	{
		Model model = ModelFactory.createDefaultModel();
		ArrayList<String> crawledSites;
		SemanticCrawlerImpl crawler = new SemanticCrawlerImpl();
		
		
		crawler.search(model, resourceURI);
		
		crawledSites = crawler.getCrawledSites(); 
		int statements = 0;
		int anonymous = 0;
		
		for(Statement s: model.listStatements().toList())
		{
			if(s.getSubject().isAnon())
				anonymous++;
			else
				statements++;
			
			if(!crawledSites.contains(s.getSubject().getURI()))
				return false;
		}
		
		try {
			Writer writer = new FileWriter("C:\\Users\\Edgla\\Desktop\\" + name + ".txt");
		
			model.write(writer , "Turtle");			
		} catch (Exception e) {
			// TODO: handle exception
		}

		System.out.println("Crawl Count: " + crawledSites.size());
		System.out.println("Statements: " + statements);
		System.out.println("Anonymous: " + anonymous + "\n");
		
		return true;
	}
	
	Model getModel(String resourceURI)
	{
		Model model = ModelFactory.createDefaultModel();
		SemanticCrawlerImpl crawler = new SemanticCrawlerImpl();
		
		crawler.search(model, resourceURI);
		
		return model;
	}

}
