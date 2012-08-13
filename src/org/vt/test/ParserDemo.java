package org.vt.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

class ParserDemo {

  public static void main(String[] args) {
    LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    if (args.length > 0) {
      demoDP(lp, args[0]);
    } else {
//      demoAPI(lp);
    	demoArticles(lp);
    }
  }

  public static void demoArticles(LexicalizedParser lp)
  {
	  String article = "Mexico Will See as Many as 12 IPOs This Year, JPMorgan\u0027s Cepeda Predicts\nBy Andres R. Martinez and Thomas Black - 2010-04-22T18:55:09Z\nMexico may have 10 to 12 initial public offerings this year, ending a 22-month drought, said Eduardo Cepeda, head of JPMorgan Chase \u0026 Co.抯 local unit.\nInvestors view Mexico as 揷heaper?than Brazil and benefiting from a U.S. economic recovery, he said at a conference in Acapulco today. Mexico won抰 have as many IPOs as Brazil, which is 揼oing ballistic,?he said.\n揚eople are realizing that Mexico is actually cheaper than Brazil today,?said Cepeda. 揃uying Mexican paper is actually a good deal. The potential for asset prices appreciating is very high.擻nGenomma Lab Internacional SAB raised $233.7 million and the Bolsa Mexicana de Valores SAB sold $443.5 million in June 2008, the last time Mexican companies sold shares.\nFor Related News and Information: Top Stories:TOP\u003cGO\u003e Link to Company News:JPM US \u003cEquity\u003e CN \u003cGO\u003e Top Latin America stories: TOPL \u003cGO\u003e Stories on IPOs in Mexico: TNI IPO MEX \u003cGO\u003e\nMore News:\n";
	  InputStream is = new ByteArrayInputStream(article.getBytes());
	  DocumentPreprocessor dp = new DocumentPreprocessor(new InputStreamReader(is));
	  TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	  GrammaticalStructureFactory  gsf = tlp.grammaticalStructureFactory();
	  for (List<HasWord> sentence : dp) {
	      Tree parse = lp.apply(sentence);
	      parse.pennPrint();
	      System.out.println();
	      
	      GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	      Collection tdl = gs.typedDependenciesCCprocessed(true);
	      System.out.println(tdl);
	      System.out.println();
	    }
  }
  
  public static void demoDP(LexicalizedParser lp, String filename) {
    // This option shows loading and sentence-segment and tokenizing
    // a file using DocumentPreprocessor
    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    // You could also create a tokenier here (as below) and pass it
    // to DocumentPreprocessor
    for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
      Tree parse = lp.apply(sentence);
      parse.pennPrint();
      System.out.println();
      
      GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
      Collection tdl = gs.typedDependenciesCCprocessed(true);
      System.out.println(tdl);
      System.out.println();
    }
  }

  public static void demoAPI(LexicalizedParser lp) {
    // This option shows parsing a list of correctly tokenized words
    String[] sent = { "This", "is", "an", "easy", "sentence", "." };
    List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
    Tree parse = lp.apply(rawWords);
    parse.pennPrint();
    System.out.println();


    // This option shows loading and using an explicit tokenizer
    String sent2 = "This is another sentence.";
    TokenizerFactory<CoreLabel> tokenizerFactory = 
      PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    List<CoreLabel> rawWords2 = 
      tokenizerFactory.getTokenizer(new StringReader(sent2)).tokenize();
    parse = lp.apply(rawWords2);

    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
    System.out.println(tdl);
    System.out.println();

    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
    tp.printTree(parse);
  }

  private ParserDemo() {} // static methods only

}
