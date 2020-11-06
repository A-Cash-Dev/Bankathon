package com.Spring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class UserRepository {
	Connection conn = null;
	RedisTemplate<String, String> redisTemp;
	ValueOperations<String, String> valueOpr;

	public UserRepository(RedisTemplate<String, String> redisTemplate) {
		String url = "jdbc:mysql://localhost:3306/usermanagement";
		String username = "root";
		String pwd = "root";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, username, pwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		redisTemp = redisTemplate;
		valueOpr = redisTemplate.opsForValue();
	}

	public int createUser(User usrObj) {
		String sql = "Insert into user_info(name, email, mobile, address) values(?,?,?,?)";
		String sql2 = "Insert into user_login(uid, password) values(?,?)";
		ResultSet rs;
		try {
			PreparedStatement pst = conn.prepareStatement(sql,
					PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setString(1, usrObj.getName());
			pst.setString(2, usrObj.getEmail());
			pst.setString(3, usrObj.getMobile());
			pst.setString(4, usrObj.getAddress());
			if (pst.executeUpdate() != 1) {
				throw new RuntimeException("No row updated");
			}
			rs = pst.getGeneratedKeys();
			if (rs.next()) {
				usrObj.setUserid(rs.getInt(1));
			} else {
				throw new RuntimeException("Could not get User id");
			}
			rs.close();
			pst.close();
			pst = conn.prepareStatement(sql2);
			pst.setInt(1, usrObj.getUserid());
			pst.setString(2, usrObj.getPassword());
			if (pst.executeUpdate() != 1) {
				conn.rollback();
				throw new RuntimeException("Could not get User id");
			}
			pst.close();
			return usrObj.getUserid();

		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public String verifyCredentials(User user) {
		String sql = "Select * from user_login where uid=? AND password=?";
		String sql2 = "Select * from user_info where user_id=?";
		String token = user.getUserid() + "_" + user.getPassword();
		try {
			if (valueOpr.get(token) != null) {
				return "Logged in successfully and getting data from redis with "
						+ valueOpr.get(token);
			}
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, user.getUserid());
			pst.setString(2, user.getPassword());
			if (pst.executeQuery().next()) {
				pst.close();
				pst = conn.prepareStatement(sql2);
				pst.setInt(1, user.getUserid());
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					user.setName(rs.getString("name"));
					user.setEmail(rs.getString("email"));
					user.setMobile(rs.getString("mobile"));
					user.setAddress(rs.getString("address"));
					rs.close();
					pst.close();
					valueOpr.set(token, user.toString(), 5, TimeUnit.MINUTES);
					return "Logged in successfully with " + user.toString();
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			return "Incorrect user id or password.";
		}
		return "Incorrect user id or password.";
	}

	public Long countLoggedInUsers() {
		String sql = "select uid, password from user_login";
		List<String> keys = new ArrayList<>();
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				keys.add(rs.getInt("uid") + "_" + rs.getString("password"));
			}
			rs.close();
			pst.close();
			return redisTemp.countExistingKeys(keys);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return 0L;
	}
	
	public List<User> signedUpUsers() {
 		String sql = "Select * from user_info";
 		List<User> signedUpusers = new ArrayList<>();
 		User user;
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				user = new User();
				user.setUserid(rs.getInt(1));
				user.setName(rs.getString(2));
				user.setEmail(rs.getString(2));
				user.setMobile(rs.getString(2));
				user.setAddress(rs.getString(2));
				signedUpusers.add(user);
				user = null;
			}
			rs.close();
			pst.close();
			return signedUpusers;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return null;

	}
	
	public int createBudget(User usrObj, Map<String, String> userBudget) {
		String sql = "Insert into user_info(name, email, mobile, address) values(?,?,?,?)";
		String sql2 = "Insert into user_budget(uid, categories, amt) values(?,?)";
		ResultSet rs;
		try {
			PreparedStatement pst = conn.prepareStatement(sql,
					PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setString(1, usrObj.getName());
			pst.setString(2, usrObj.getEmail());
			pst.setString(3, usrObj.getMobile());
			pst.setString(4, usrObj.getAddress());
			if (pst.executeUpdate() != 1) {
				throw new RuntimeException("No row updated");
			}
			rs = pst.getGeneratedKeys();
			if (rs.next()) {
				usrObj.setUserid(rs.getInt(1));
			} else {
				throw new RuntimeException("Could not get User id");
			}
			rs.close();
			pst.close();
			pst = conn.prepareStatement(sql2);
			pst.setInt(1, usrObj.getUserid());
			pst.setString(2, userBudget.keySet().toString());
			pst.setString(3, userBudget.values().toString());
			if (pst.executeUpdate() != 1) {
				conn.rollback();
				throw new RuntimeException("Could not get User id");
			}
			pst.close();
			return usrObj.getUserid();

		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public Map<String, String> viewBudget(int uid) {
		String sql = "select * from user_budget where uid = ?";
		Map<String,String> userbudget = new HashMap<String, String>();
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, uid);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				userbudget.put(rs.getString("categories"),rs.getString("amt"));
			}
			rs.close();
			pst.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return userbudget;
	}
	
	public boolean addSmartBudget(User usrObj, Map<String, String> smartBudget){
		String sql = "Insert into smart_budget(id, biller, amt) values(?,?,?)";
		try {
			PreparedStatement pst = conn.prepareStatement(sql,
					PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setInt(1, usrObj.getUserid());
			pst.setString(2, smartBudget.keySet().toString());
			pst.setString(3, smartBudget.values().toString());
			if (pst.executeUpdate() != 1) {
				throw new RuntimeException("No row updated");
			}
			if (pst.executeUpdate() != 1) {
				conn.rollback();
				throw new RuntimeException("Could not get User id");
			}
			valueOpr.set(String.valueOf(usrObj.getUserid()), smartBudget.keySet().toString()+"__"+smartBudget.values().toString());
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Map<String, String> viewSmartBudget(int uid) {
		Map<String,String> smartBudget = new HashMap<String, String>();
		try {
			String key_Value = valueOpr.get(String.valueOf(uid));
			String smartBudgetArray[] = key_Value.split("__");
			smartBudget.put(smartBudgetArray[0],smartBudgetArray[1]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return smartBudget;
	}
}


