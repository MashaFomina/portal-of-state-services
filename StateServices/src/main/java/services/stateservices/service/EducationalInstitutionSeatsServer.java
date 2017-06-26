package services.stateservices.service;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import services.stateservices.facade.Facade;

class Institution {
    String title;
    String city;
    String district;
    String telephone;
    String fax;
    String address;
    List<String> seats;
    List<String> busySeats;
}

public class EducationalInstitutionSeatsServer {

    private static Facade facade = new Facade();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/get", new GetHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("The server is running");
    }

    // http://localhost:8080/get?edu_inst_id=1
    static class GetHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange) throws IOException {
            StringBuilder response = new StringBuilder();
            Map <String,String> parms = EducationalInstitutionSeatsServer.queryToMap(httpExchange.getRequestURI().getQuery());
            try {
                Gson gson = new Gson();
                Institution institution = EducationalInstitutionSeatsServer.getInstitutionImpl(new Integer(parms.get("edu_inst_id")));
                response.append(gson.toJson(institution));
            } catch (Exception e) {
                response.append("error");
            }
            EducationalInstitutionSeatsServer.writeResponse(httpExchange, response.toString());
        }
    }

    public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


    /**
     * returns the url parameters in a map
     * @param query
     * @return map
     */
    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }

    private static Institution getInstitutionImpl(int id) throws Exception {
        Institution institution = new Institution();
        Map<Integer, Integer> seats = facade.getEducationalInstitutionSeats(id);
        Map<Integer, Integer> busySeats = facade.getEducationalInstitutionBusySeats(id);

        if (seats == null || busySeats == null) {
            throw new Exception("Error with getting information about institution!");
        }
        
        institution.title = facade.getEducationalInstitutionTitle(id);
        institution.city = facade.getEducationalInstitutionCity(id);
        institution.district = facade.getEducationalInstitutionDistrict(id);
        institution.telephone = facade.getEducationalInstitutionTelephone(id);
        institution.fax = facade.getEducationalInstitutionFax(id);
        institution.address = facade.getEducationalInstitutionAddress(id);
        institution.seats = seats.entrySet().stream().
                map(pair -> pair.getKey() + ":"+ pair.getValue()).
                collect(Collectors.toList());
        institution.busySeats = busySeats.entrySet().stream().
                map(pair -> pair.getKey() + ":"+ pair.getValue()).
                collect(Collectors.toList());
        return institution;
    }

}