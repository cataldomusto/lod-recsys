package org.semanticweb.yars.nx.cli;

import org.apache.commons.cli.*;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.util.CallbackNxBufferedWriter;

import java.io.*;
import java.util.Iterator;
import java.util.logging.Logger;

public class Head {
    static transient Logger _log = Logger.getLogger(Head.class.getName());

    /**
     * @param args
     * @throws IOException
     * @throws org.semanticweb.yars.nx.parser.ParseException
     */
    public static void main(String[] args) throws org.semanticweb.yars.nx.parser.ParseException, IOException {
        NxParser.DEFAULT_PARSE_DTS = false;

        Options options = Main.getStandardOptions();

        Option headO = new Option("p", "percent of data in head; e.g., -h 25");
        headO.setArgs(1);
        headO.setRequired(true);
        options.addOption(headO);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters:", options);
            return;
        }

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters:", options);
            return;
        }

        InputStream is = Main.getMainInputStream(cmd);

        OutputStream os = Main.getMainOutputStream(cmd);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        Callback cb = new CallbackNxBufferedWriter(bw);

        int ticks = Main.getTicks(cmd);
        double head = Double.parseDouble(cmd.getOptionValue("p"));

        if (head > 100 || head < 0) {
            _log.severe("Head must be greater than 0 and less than 100, not " + head + ".");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters:", options);
            return;
        }
        head = head / 100d;

        Iterator<Node[]> it = new NxParser(is);

        _log.info("Counting triples in input...");
        long c = 0;
        while (it.hasNext()) {
            it.next();
            c++;
            if (ticks > 0 && c % ticks == 0) {
                _log.info("Counted " + c + " triples in input...");
            }
        }

        _log.info(c + " triples in input...");

        is.close();
        is = Main.getMainInputStream(cmd);
        it = new NxParser(is);

        long top = Math.round((double) c * head);
        _log.info("Writing " + top + " triples to output...");

        c = 0;
        while (it.hasNext() && c < top) {
            cb.processStatement(it.next());
            c++;
            if (ticks > 0 && c % ticks == 0) {
                _log.info("Buffered " + c + " triples to output...");
            }
        }

        _log.info("Buffered " + c + " final triples to output.");

        is.close();
        bw.close();
    }
}
