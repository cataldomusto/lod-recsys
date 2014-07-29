package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.entity.RequestStruct;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by asuglia on 7/1/14.
 */
public class GraphFactory {

    public static Pair<RecGraph, RequestStruct> create(String specificModel, Object... params) throws IOException {
        RecGraph graph = null;
        RequestStruct requestStruct = null;

        switch (specificModel) {
            case "UserItemGraph":
                graph = new UserItemGraph((String) params[0], (String) params[1]);
                requestStruct = RequestStructFactory.create(specificModel);
                break;
            case "UserItemPriorGraph":
                graph = new UserItemPriorGraph((String) params[0], (String) params[1]);
                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
                break;

            case "UserItemProperty":
                graph = new UserItemProperty((String) params[0],
                        (String) params[1],
                        (String) params[3],
                        (List<MovieMapping>) params[4]);
                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
                break;

            case "UserItemPropTag":
                graph = new UserItemPropTag((String) params[0],
                        (String) params[1],
                        (String) params[3],
                        (List<MovieMapping>) params[4],
                        (Map<String, List<String>>) params[5]);
                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
                break;
            case "UserItemTag":
                graph = new UserItemTag((String) params[0],
                        (String) params[1],
                        (List<MovieMapping>) params[4],
                        (Map<String, List<String>>) params[5]);
                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
                break;

            case "UserItemOneExp":
                graph = new UserItemOneExp((String) params[0],
                        (String) params[1],
                        (String) params[3],
                        (List<MovieMapping>) params[4]
                );
                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
                break;
            case "UserItemComplete":
                graph = new UserItemComplete((String) params[0],
                        (String) params[1],
                        (String) params[3],
                        (List<MovieMapping>) params[4],
                        (Map<String, List<String>>) params[5]
                );
                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
                break;

            case "UserItemJaccard":
                graph = new UserItemJaccard((String) params[0],
                        (String) params[1],
                        (String) params[3],
                        (List<MovieMapping>) params[4]);
                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);


        }

        return new Pair<>(graph, requestStruct);


    }

}


class RequestStructFactory {
    public static RequestStruct create(String specificModel, Object... params) throws IOException {

        switch (specificModel) {
            case "UserItemGraph":
                return new RequestStruct();
            case "UserItemPriorGraph":
                return new RequestStruct((double) params[0]);
            case "UserItemProperty":
                return new RequestStruct((double) params[0]);
            case "UserItemPropTag":
                return new RequestStruct((double) params[0]);
            case "UserItemTag":
                return new RequestStruct((double) params[0]);
            case "UserItemOneExp":
                return new RequestStruct((double) params[0]);
            case "UserItemComplete":
                return new RequestStruct((double) params[0]);
            case "UserItemJaccard":
                return new RequestStruct((double) params[0]);
            default:
                return null;

        }


    }
}
