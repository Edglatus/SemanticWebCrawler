package impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;

import crawler.SemanticCrawler;

public class SemanticCrawlerImpl implements SemanticCrawler {
	private ArrayList<String> crawledSites = new ArrayList<String>();
	
	public ArrayList<String> getCrawledSites() { return this.crawledSites; };
	
	@Override
	public void search(Model graph, String resourceURI) {
		ArrayList<String> crawledSites = new ArrayList<String>();
		
		graph.add(getResourceModel(resourceURI, crawledSites));
		this.crawledSites = crawledSites;
	}
	
	// Busca o recurso especificado, e retorna um modelo com suas triplas.
	private Model getResourceModel(String resourceURI, ArrayList<String> crawledSites)
	{
		crawledSites.add(resourceURI);
		
		Model createdModel = ModelFactory.createDefaultModel();
		Model foundModel = readURIModel(resourceURI);
		Resource resource = ResourceFactory.createResource(resourceURI);
		
		createdModel.add(getEquivalentResourceModel(foundModel, resource, crawledSites));
		createdModel.add(getResourceStatements(foundModel, resource));
		
		return createdModel;
	}
	
	// Pega o arquivo RDF com base na URI. Retorna o Modelo.
	private Model readURIModel(String resourceURI)
	{
		Model searchModel = ModelFactory.createDefaultModel();
		
		searchModel.read(resourceURI);
		
		return searchModel;
	}
	
	// Cria iteradores para o recurso provido no OWL:SameAs como Subject e Object
	private Model getEquivalentResourceModel(Model foundModel, Resource resource, ArrayList<String> crawledSites)
	{
		Model createdModel = ModelFactory.createDefaultModel();
		
		Iterator<Statement> sameAsSubject = foundModel.listStatements(resource, OWL.sameAs, (Resource) null);
		Iterator<Statement> sameAsObject = foundModel.listStatements(null, OWL.sameAs, resource);
		
		createdModel.add(crawlForEquivalentModels(sameAsSubject, crawledSites));
		createdModel.add(crawlForEquivalentModels(sameAsObject, crawledSites));
		
		return createdModel;
	}
	
	// Retorna um Model com as triplas dos recursos obtidos no GetSameAsModel
	private Model crawlForEquivalentModels(Iterator<Statement> statements, ArrayList<String> crawledSites)
	{
		Model createdModel = ModelFactory.createDefaultModel();
		Statement statement;
		Resource foundResource, foundSubject;
		String resourceURI, subjectURI;
		
		while(statements.hasNext())
		{
			statement = statements.next();
			foundResource = statement.getResource();
			foundSubject = statement.getSubject();
			
			resourceURI = (foundResource.isURIResource()) ? foundResource.getURI() : null;
			subjectURI = (foundSubject.isURIResource()) ? foundSubject.getURI() : null;
			
			if(resourceURI != null && !crawledSites.contains(resourceURI))
				crawl(createdModel, resourceURI);
			
			if(subjectURI != null && !crawledSites.contains(subjectURI))
				crawl(createdModel, subjectURI);					
		}
		return createdModel;
	}
	
	private void crawl(Model model, String foundURI)
	{
		try {
			System.out.println(foundURI);
			model.add(getResourceModel(foundURI, crawledSites));
		} catch (Exception e) {
			System.out.println(" not Found\n");
		}
	}
	
	// Retorna um modelo com os Statements do modelo RDF, cujo sujeito for o recurso providenciado.
	private Model getResourceStatements(Model foundModel, Resource resource)
	{
		Model createdModel = ModelFactory.createDefaultModel();
		Statement foundStatement;
		RDFNode foundResource;
		
		Iterator<Statement> foundStatements = foundModel.listStatements(resource, null, (RDFNode)null);
		
		while(foundStatements.hasNext())
		{
			foundStatement = foundStatements.next();
			foundResource = foundStatement.getObject();
			
			// Recursivamente adiciona recursos anônimos.
			if (foundResource.isAnon())
				createdModel.add(getResourceStatements(foundModel, (Resource) foundResource));
			
			createdModel.add(foundStatement);
		}
		
		return createdModel;
	}
	
}
