package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;

/**
 * This servlet should handle urls beginning with api.arachb.org/behavior
 * @author pmidford
 *
 */
public class Behavior extends HttpServlet {
	


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

		final OutputStream os = response.getOutputStream();

		String name = request.getQueryString();
		//System.out.println("raw string is: |" + name);
		name = name.substring("behavior=".length()).trim();
		final String[] components = name.split("\\+");
		if (components.length>1){
			StringBuilder b = new StringBuilder();
			for (String c : components){
				b.append(c);
				b.append(' ');
			}
			name = b.toString().trim();  //remove final space
		}
		if (!validateBehaviorName(name)){
			Util.returnError(os);
			os.flush();
			os.close();
			return;			
		}
		response.setContentType("application/sparql-results+json");
		String repositoryId = "test1";
		Repository repo = null;
		RepositoryConnection con = null;
		LocalRepositoryManager manager = new LocalRepositoryManager(new File(Util.ADUNAHOME));

		try {
			manager.initialize();
			repo = manager.getRepository(repositoryId);
			con = repo.getConnection();
			String behaviorQueryString = getName2BehaviorReportQuery(name);
			System.out.println("ethogram query = \n" + behaviorQueryString);
			if (!Util.tryQuery(behaviorQueryString,con,os)){
				String taxonIdQueryString = getName2BehaviorIdQuery(name);
				if (!Util.tryQuery(taxonIdQueryString,con,os)){
					Util.returnError(os);
				}
			}
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try{
				con.close();
				repo.shutDown();
			}
			catch (RepositoryException e){
				System.out.println("Error while trying to close repository");
				e.printStackTrace();
			}
		}
		os.flush();
		os.close();
    }
    
    
    void jsonFormatResult(TupleQueryResult r,OutputStream os){
		TupleQueryResultFormat jsonFormat = QueryResultIO.getWriterFormatForMIMEType("application/sparql-results+json");
		final TupleQueryResultWriter jsonResults = QueryResultIO.createWriter(jsonFormat, os);
		try{
			jsonResults.startQueryResult(r.getBindingNames());
			while(r.hasNext()){
				jsonResults.handleSolution(r.next());
			}
			jsonResults.endQueryResult();  
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (TupleQueryResultHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public boolean validateBehaviorName(String name){
    	String[]nameList = name.split(" ");
    	return (nameList.length<=3);
    }

    
    String getName2BehaviorReportQuery(String name){
    	final StringBuilder b = new StringBuilder();
    	b.append(Util.OBOPREFIX);
    	b.append("SELECT ?behavior ?taxon ?anatomy\n");
        b.append("WHERE {?behavior_id rdfs:label \"%s\"^^xsd:string . \n");
        b.append("       ?behavior_id rdfs:label ?behavior . \n ");
        b.append("       ?s8 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?behavior_id . \n");
        b.append("       ?s8 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?s7 . \n");
        b.append("       ?s7 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?s6 .\n");
        b.append("       ?s6 <http://www.w3.org/2002/07/owl#someValuesFrom> ?s5 . \n ");
        b.append("       ?s5 <http://www.w3.org/2002/07/owl#intersectionOf> ?s4 . \n");
        b.append("       ?s4 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?anatomy_id .  \n");
        b.append("       ?s4 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?s2 . \n");
        b.append("       ?s2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?s1 .  \n");
        b.append("       ?s1 <http://www.w3.org/2002/07/owl#someValuesFrom> ?taxon_id .  \n");
        b.append("       ?taxon_id rdfs:label ?taxon . \n");
        b.append("       ?anatomy_id rdfs:label ?anatomy . }\n");
        
        return String.format(b.toString(),name);
    }
    
    String getName2BehaviorIdQuery(String name){
    	final StringBuilder b = new StringBuilder();
    	b.append(Util.OBOPREFIX);
    	b.append("SELECT ?behavior_name ?behavior_id \n");
        b.append("WHERE {?behavior_id rdfs:label \"%s\"^^xsd:string . \n");
        b.append("       ?behavior_id rdfs:label ?behavior_name . }\n ");
    	return String.format(b.toString(), name);
    }


}
