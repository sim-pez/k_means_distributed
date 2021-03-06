
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;


import java.io.IOException;


import java.util.ArrayList;
import java.util.StringTokenizer;


//assign each point to closest cluster
public class Map extends Mapper<Object, Text, Centroid, Point> {
    private ArrayList<Centroid> centroids = new ArrayList<>();

    //get centroids from conf
    @Override
    protected void setup(Context context) {
        Configuration conf = context.getConfiguration();
        Gson gson = new Gson();
        int k = Integer.parseInt(conf.get("k"));
        for (int i = 0; i < k; i++){
            String c_string = conf.get(String.valueOf(i));
            Centroid c = gson.fromJson(c_string, Centroid.class); //deserialize
            centroids.add(i, c);
        }
    }

    public void map(Object key, Text coord, Context context) throws IOException, InterruptedException {

        StringTokenizer tokenizer = new StringTokenizer(coord.toString(), ",");

        float x = Float.parseFloat(tokenizer.nextToken());
        float y = Float.parseFloat(tokenizer.nextToken());
        float z = Float.parseFloat(tokenizer.nextToken());
        Point p = new Point(x, y, z);

        //choose the closest centroid
        float minDistance = Float.MAX_VALUE;
        Centroid bestCentroid = centroids.get(0);
        for(Centroid c: centroids){
            float d = c.getDistance(p);
            if (d < minDistance){
                bestCentroid = c;
                minDistance = d;
            }
        }

        context.write(bestCentroid, p);
    }
}