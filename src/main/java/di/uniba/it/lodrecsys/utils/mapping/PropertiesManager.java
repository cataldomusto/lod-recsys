package di.uniba.it.lodrecsys.utils.mapping;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.tdb.TDBFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asuglia on 6/26/14.
 */
public class PropertiesManager {
    private Dataset tupleDataset;
    private Model datasetModel;
    private boolean isWriteMode;


    public PropertiesManager(String dirName) {
        tupleDataset = TDBFactory.createDataset(dirName);
        datasetModel = tupleDataset.getDefaultModel();

    }

    public void start(boolean isWriteMode) {
        this.isWriteMode = isWriteMode;
        tupleDataset.begin(isWriteMode ? ReadWrite.WRITE : ReadWrite.READ);
        datasetModel = tupleDataset.getDefaultModel();

    }

    public void addSolution(QuerySolution currSolution, String subjectURI) {
        if (isWriteMode) {
            Resource currResource = datasetModel.createResource(subjectURI);

            Property prop = datasetModel.createProperty(currSolution.getResource("?prop").toString());
            Statement stat = datasetModel.createStatement(currResource, prop, currSolution.get("?value").toString());
            datasetModel.add(stat);

        }
    }

    public void addSolution(QuerySolution currSolution) {
        if (isWriteMode) {
            Resource currResource = datasetModel.createResource(currSolution.getResource("?exp_prop_value").toString());
            Property prop = datasetModel.createProperty(currSolution.getResource("?exp_prop").toString());
            Statement stat = datasetModel.createStatement(currResource, prop, currSolution.get("?item"));
            datasetModel.add(stat);

        }

    }

    public List<Statement> getResourceProperties(String resourceURI) {
        List<Statement> listStat = null;

        if (!isWriteMode) {

            listStat = new ArrayList<>();

            StmtIterator statIterator = datasetModel.getResource(resourceURI).listProperties();

            while (statIterator.hasNext()) {
                listStat.add(statIterator.nextStatement());
            }
        }

        return listStat;
    }

    public void commitChanges() {
        tupleDataset.commit();
    }

    public void closeManager() {
        tupleDataset.end();
    }

    public void restart() {
        if (isWriteMode)
            tupleDataset.begin(ReadWrite.WRITE);
    }


}
