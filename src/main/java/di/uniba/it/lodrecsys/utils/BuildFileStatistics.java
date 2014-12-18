package di.uniba.it.lodrecsys.utils;

import org.apache.commons.math3.stat.StatUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pierpaolo
 */
public class BuildFileStatistics {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            reader.readLine();
            String line;
            String[] split;
            Map<String, ObjectStatistics> items = new HashMap<String, ObjectStatistics>();
            Map<String, ObjectStatistics> users = new HashMap<String, ObjectStatistics>();
            while (reader.ready()) {
                line = reader.readLine();
                split = line.split(",");
                String userId = split[0];
                String itemId = split[1];
                ObjectStatistics item = items.get(itemId);
                if (item == null) {
                    item = new ObjectStatistics(itemId);
                    items.put(itemId, item);
                }
                ObjectStatistics user = users.get(userId);
                if (user == null) {
                    user = new ObjectStatistics(userId);
                    users.put(userId, user);
                }
                if (split[2].equals("1")) {
                    item.setPos(item.getPos() + 1);
                    user.setPos(user.getPos() + 1);
                } else {
                    item.setNeg(item.getNeg() + 1);
                    user.setNeg(user.getNeg() + 1);
                }
            }
            reader.close();
            List<ObjectStatistics> list = new ArrayList<ObjectStatistics>(items.values());
            Collections.sort(list);
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[1] + ".item"));
            writer.append("ItemId\tpos\tneg");
            writer.newLine();
            for (ObjectStatistics e : list) {
                writer.append(e.getId()).append("\t").append(String.valueOf(e.getPos())).append("\t").append(String.valueOf(e.getNeg()));
                writer.newLine();
            }
            writer.close();
            double[] iv = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                iv[i] = list.get(i).getPos();
            }
            double mean = StatUtils.mean(iv);
            double var = StatUtils.variance(iv, mean);
            System.out.println("Mean (item pos): " + mean);
            System.out.println("Variance (item pos): " + var);
            iv = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                iv[i] = list.get(i).getNeg();
            }
            mean = StatUtils.mean(iv);
            var = StatUtils.variance(iv, mean);
            System.out.println("Mean (item neg): " + mean);
            System.out.println("Variance (item neg): " + var);
            List<ObjectStatistics> listU = new ArrayList<ObjectStatistics>(users.values());
            Collections.sort(listU);
            writer = new BufferedWriter(new FileWriter(args[1] + ".user"));
            writer.append("UserId\tpos\tneg");
            writer.newLine();
            for (ObjectStatistics u : listU) {
                writer.append(u.getId()).append("\t").append(String.valueOf(u.getPos())).append("\t").append(String.valueOf(u.getNeg()));
                writer.newLine();
            }
            writer.close();
            iv = new double[listU.size()];
            for (int i = 0; i < listU.size(); i++) {
                iv[i] = listU.get(i).getPos();
            }
            mean = StatUtils.mean(iv);
            var = StatUtils.variance(iv, mean);
            System.out.println("Mean (user pos): " + mean);
            System.out.println("Variance (user pos): " + var);
            iv = new double[listU.size()];
            for (int i = 0; i < listU.size(); i++) {
                iv[i] = listU.get(i).getNeg();
            }
            mean = StatUtils.mean(iv);
            var = StatUtils.variance(iv, mean);
            System.out.println("Mean (user neg): " + mean);
            System.out.println("Variance (user neg): " + var);
        } catch (IOException ioex) {
            Logger.getLogger(BuildFileStatistics.class.getName()).log(Level.SEVERE, null, ioex);
        }

    }

}
