package org.vt.datapreprocess;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.vt.entity.Article;
import org.vt.entity.ArticleStructure;
import org.vt.entity.CompanyMatchedArticles;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * This program using Standford 
 * @author wei
 *
 */
public class ParseNews {
	private LexicalizedParser lp;
	private LinkedList<CompanyMatchedArticles> cmArticles;
	private Properties properties;
	
	public ParseNews() throws FileNotFoundException, IOException
	{
		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		properties = new Properties();
		properties.load(new FileInputStream("config.property"));
	}
	
	public void initiateArticles() throws JsonIOException, JsonSyntaxException, IOException
	{
		Type listType = new TypeToken<LinkedList<CompanyMatchedArticles>>(){}.getType();
		String filePath = properties.getProperty("newsPath");
		Gson gson = new Gson();
		cmArticles = gson.fromJson(new FileReader(filePath), listType);
		LinkedList<ArticleStructure> lla = new LinkedList<ArticleStructure>();
		for(CompanyMatchedArticles cma:cmArticles)
		{
			System.out.println(cma.getStockIndex());
			for(Article article:cma.getArticles())
			{
				System.out.println(article.getArticelId());
				ArticleStructure as = parseArticles(article.getArticelId(),article.getContent());
				lla.add(as);
			}
		}
		
		Gson gson2 = new Gson();
		String json = gson2.toJson(lla);
		BufferedWriter bw = new BufferedWriter(new FileWriter("d:/1.txt"));
		bw.write(json);
		bw.flush();
		bw.close();
	}
	
	public ArticleStructure parseArticles(String articleId,String content)
	  {
		  ArticleStructure as = new ArticleStructure(articleId);
		  
		  InputStream is = new ByteArrayInputStream(content.getBytes());
		  DocumentPreprocessor dp = new DocumentPreprocessor(new InputStreamReader(is));
		  TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		  GrammaticalStructureFactory  gsf = tlp.grammaticalStructureFactory();
		  for (List<HasWord> sentence : dp) {
		      Tree parse = lp.apply(sentence);
//		      parse.pennPrint();
//		      System.out.println();
		      
		      GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		      List<TypedDependency> tdl = gs.typedDependenciesCCprocessed(true);
		      for(TypedDependency td:tdl)
		      {
		    	  as.parts.add(td.toString());
		      }
		    }
		  return as;
	  }

	public static void main(String[] args) {
		try {
			ParseNews pn = new ParseNews();
			pn.initiateArticles();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
