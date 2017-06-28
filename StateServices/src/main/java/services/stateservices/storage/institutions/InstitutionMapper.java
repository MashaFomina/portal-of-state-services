package services.stateservices.storage.institutions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.institutions.Institution;

abstract class InstitutionMapper {
    protected static Connection connection;
    protected static ArrayList<String> cities = new ArrayList<String>();
    protected static Map<String, ArrayList<String>> districts = new HashMap<>();
    
    public ArrayList<String> getCities() throws SQLException {
        if (!cities.isEmpty()) return cities;

        String selectSQL = "SELECT DISTINCT city FROM institutions WHERE is_edu = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, getInstitutionType());
        ResultSet rs = selectStatement.executeQuery();

        while (rs.next()) {
            cities.add(rs.getString("city"));
        }

        selectStatement.close();
        
        return cities;
    }
    
    public ArrayList<String> getCityDistricts(String city) throws SQLException {
        if (districts.containsKey(city)) return districts.get(city);

        String selectSQL = "SELECT DISTINCT district FROM institutions WHERE city = ? and is_edu = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setString(1, city);
        selectStatement.setInt(2, getInstitutionType());
        ResultSet rs = selectStatement.executeQuery();

        ArrayList<String> cityDistricts = new ArrayList<String>();
        while (rs.next()) {
            cityDistricts.add(rs.getString("district"));
        }
        districts.put(city, cityDistricts);

        selectStatement.close();
        
        return districts.get(city);
    }
    
    public abstract Institution findByID(int id) throws SQLException;
    public abstract int getInstitutionType();
    
    public void insertItem(Institution item) throws SQLException {
        String insertSQL = "INSERT INTO institutions (title, city, district, telephone, fax, address, is_edu) VALUES (?, ?, ?, ?, ?, ?, ?);";
        PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
        String city = item.getCity();
        String district = item.getDistrict();
        insertStatement.setString(1, item.getTitle());
        insertStatement.setString(2, city);
        insertStatement.setString(3, district);
        insertStatement.setString(4, item.getTelephone());
        insertStatement.setString(5, item.getFax());
        insertStatement.setString(6, item.getAddress());
        insertStatement.setInt(7, getInstitutionType());
        insertStatement.execute();
        ResultSet rs = insertStatement.getGeneratedKeys();
        if (rs.next()) {
            long id = rs.getLong(1);
            item.setId((int) id);
        }

        insertStatement.close();

        if (!cities.isEmpty() && !cities.contains(city)) {
            cities.add(city);
        }
        if (districts.containsKey(city) && !districts.get(city).contains(district)) {
            districts.get(city).add(district);
        }
    }
    
    public void updateItem(Institution item) throws SQLException {
        if (item.isUpdated()) {
                String updateSQL = "UPDATE institutions SET title = ?, city = ?, district = ?, telephone = ?, fax = ?, address = ? WHERE id = ?;";
                PreparedStatement updateStatement = connection.prepareStatement(updateSQL);
                updateStatement.setString(1, item.getTitle());
                updateStatement.setString(2, item.getCity());
                updateStatement.setString(3, item.getDistrict());
                updateStatement.setString(4, item.getTelephone());
                updateStatement.setString(5, item.getFax());
                updateStatement.setString(6, item.getAddress());
                updateStatement.setInt(7, item.getId());
                updateStatement.execute();
        }
        item.resetUpdated();
    }
}
