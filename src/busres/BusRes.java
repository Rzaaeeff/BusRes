package busres;

import java.awt.Component;
import java.awt.Container;
import java.sql.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import com.mysql.jdbc.Driver;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Rzaaeeff
 */

public class BusRes extends javax.swing.JFrame {
    
    private static boolean canReset = false;
    
    List<String> box_data;
    List<Integer> bus_ids;
    
    private int res_id;
    
    public static String[] customer_data;
    public static boolean isDialogDone = false;

    private String  st_id = "", st_nm = "", st_fm = "", st_to = "", 
                    st_dt = "", st_dd = "", st_td = "", st_tf = "";
    private String seats;
    
    String[] cities = {"Seçin...", "Baku", "Quba", "Mingachevir", "Zaqatala", "Ganca", "Neftchala", "Astara", "Fuzuli", "Imishli"};
    final static String DATE_FORMAT = "dd/MM/yyyy";
    final static String TIME_FORMAT = "HH:mm";
    final static String NUMBER_PATTERN = "(?:\\d*)?\\d+";
    
    public BusRes() {
        initComponents();
    }
    
    // ------------------------ [ C O N N E C T I O N ] ------------------------ //
    private Connection connection = null;
    protected PreparedStatement ps = null;
    protected ResultSet rs = null;
    
    private final String DB_URL = "jdbc:mysql://localhost";
    private final String USERNAME = "USERNAME";
    private final String PASSWORD = "PASSWORD";
    
    private final String INSERT = "INSERT INTO busres.buses "
            + "(BUS_ID, BUS_NAME, SOURCE, DESTINATION, DEPARTURE_TIME, DEPARTURE_DATE, TRAVEL_DURATION, FARE) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
    private final String DELETE = "DELETE FROM busres.buses "
            + "WHERE BUS_ID = ?;";
    private final String SELECT_ALL_BUS = "SELECT * FROM busres.buses;";
    private final String SELECT_ALL_TICKET = "SELECT * FROM busres.tickets";
    private final String SELECT_BUSES_RESERVATION = "SELECT BUS_ID FROM busres.buses "
            + "WHERE SOURCE=? AND "
            + "DESTINATION=? AND "
            + "DEPARTURE_DATE=?;";
    private final String SELECT_BUS = "SELECT * FROM busres.buses WHERE BUS_ID=?;";
    private final String INSERT_SEAT = "UPDATE busres.buses "
            + "SET SEATS=? WHERE BUS_ID=?;";
    private final String INSERT_INTO_TICKETS = "INSERT INTO busres.tickets "
            + "(BUS_ID, SEAT, C_NAME, C_SURNAME, C_PHONE_NUMBER) "
            + "VALUES (?, ?, ?, ?, ?);";
    
    private Connection connect() throws SQLException {
        DriverManager.registerDriver(new Driver());
        connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        System.out.println("Connected!");
        return connection;
    }
    
    private void disconnect() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (connection != null) {
                connection.close();
                System.out.println("Disconnected!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // ------------------------ [ C O N N E C T I O N ] ------------------------ //
    
    // Close App
    private void close() {
        Container container = jLabel1.getParent();
        do {
            container = container.getParent();
        } while(!(container instanceof JFrame));
        ((JFrame) container).dispose();
        disconnect();
    }
    
    // ------------------------ [ C H E C K I N G ] ------------------------ // 
    
    // Checking Date
    private boolean isDateValid(String date) {
        try {
            java.text.DateFormat df = new java.text.SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (java.text.ParseException e) {
            return false;
        }
    }
    
    // Checking Time
    private boolean isTimeValid(String time) {
        try {
            java.text.DateFormat tf = new java.text.SimpleDateFormat(TIME_FORMAT);
            tf.setLenient(false);
            tf.parse(time);
            return true;
        } catch (java.text.ParseException e) {
            return false;
        }
    }
    
    // Checking if numeric
    private boolean isNumeric(String num) {
        if(Pattern.matches(NUMBER_PATTERN, num)) return true;
        return false;
    } 
    
    // Checking All Fields [BUS MANAGEMENT]
    private boolean bm_isAnyEmpty() {
        if (st_id.equals("") || st_nm.equals("") || st_fm.equals("") || st_to.equals("") || st_dt.equals("") || st_dd.equals("") || st_td.equals("") || st_tf.equals("")) return true;
        return false;
    }
    
    // ------------------------ [ C H E C K I N G ] ------------------------ //
    
    // ------------------------ [ C L E A R ] ------------------------ //
    // Clear all fields [BUS MANAGEMENT]
    private void bm_clear() {
        bus_id_field.setText("");
        bus_name_field.setText("");
        from_combobox.setSelectedItem(cities[0]);
        to_combobox.setSelectedItem(cities[0]);
        departure_time_field_hour.setText("");
        departure_time_field_minute.setText("");
        departure_date_field_day.setText("");
        departure_date_field_month.setText("");
        departure_date_field_year.setText("");
        travel_duration_field.setText("");
        fare_field.setText("");
    }
    
    // Clear all fields [RESERVATION]
    private void res_clear() {
        res_from_combobox.setSelectedItem(cities[0]);
        res_to_combobox.setSelectedItem(cities[0]);
        res_departure_day.setText("");
        res_departure_month.setText("");
        res_departure_year.setText("");
    }
    
    // Clear our String variables [BUS MANAGEMENT]
    private void st_clear() {
        st_id = st_nm = st_fm = st_to = "";
        st_dt = st_dd = st_td = st_tf = "";
    }
    // ------------------------ [ C L E A R ] ------------------------ //
    
    
    // ------------------------ [ G E T  D E T A I L S ] ------------------------ //
    // Populating Our Strings [BUS MANAGEMENT]
    private void bm_getBusDetails() {
        st_id = bus_id_field.getText();
        st_nm = bus_name_field.getText();
        st_fm = from_combobox.getSelectedItem().toString();   
        st_to = to_combobox.getSelectedItem().toString();
        st_dt = departure_time_field_hour.getText() + ":" + 
                departure_time_field_minute.getText();
        st_dd = departure_date_field_day.getText() + "/" +
                departure_date_field_month.getText() + "/" +
                departure_date_field_year.getText();
        st_td = travel_duration_field.getText();
        st_tf = fare_field.getText();
    }
    
    // Populating Our Strings [RESERVATION]
    private void res_getBusDetails() {
        st_fm = res_from_combobox.getSelectedItem().toString();
        st_to = res_to_combobox.getSelectedItem().toString();
        st_dd = res_departure_day.getText() + "/" +
                res_departure_month.getText() + "/" +
                res_departure_year.getText();
    }
    
    // Populating Table
    private static DefaultTableModel buildTableModel(ResultSet rs, Vector<String> columnNames)
        throws SQLException {

    ResultSetMetaData metaData = rs.getMetaData();

    int columnCount = metaData.getColumnCount();

    // data of the table
    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
    do {
        Vector<Object> vector = new Vector<Object>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            vector.add(rs.getObject(columnIndex));
        }
        data.add(vector);
    } while (rs.next());

    return new DefaultTableModel(data, columnNames);

}
    
    // Table Names
    private static Vector<String> getColumnNames(TableModel model) {
        
        int columnCount = model.getColumnCount();
        Vector<String> columnNames = new Vector<String>();
        for(int column = 0; column < columnCount; column++) {
            columnNames.add(model.getColumnName(column));
        }
        
        return columnNames;
    }
    
    // ------------------------ [ G E T  D E T A I L S ] ------------------------ //
    
    // ------------------------ [ R E S E R V A T I O N ] ------------------------ //
    
    private static boolean[] getAvailableSeats(String seats) {
        boolean[] seat_bool = new boolean[30];
        String num = "";
        int i;

        for(i = 0; i < seat_bool.length; i++) seat_bool[i] = false;

        if(seats.length() <= 1) return seat_bool;
        else {
            for(i = 0; i < seats.length();) {
                if(seats.charAt(i) == '_') i++;
                else {
                    num += seats.substring(i, i + 2);
                    byte index = Byte.parseByte(num);

                    seat_bool[index - 1] = true;

                    num = "";
                    i += 2;
                }
            }

            return seat_bool;
        }
    }
    
    private static String reserveSeat(String seats, byte seatNumber) {
        String result = seats, num = "";
	byte i;
		
	if(result.length() <= 1) return result;
	else {
            for(i = 0; i < result.length();) {
                if(seats.charAt(i) == '_'){
		num += result.substring(i+1, i+3); 
		if(seatNumber == Byte.parseByte(num)) result = result.replace("_" + num, "");
		num = "";
		i += 3;
		}
            }
			
            return result;
	}
    }
    
    private void populateBusPanel(String index, String id, 
            String departure_time, String travel_duration) {
        res_index_label.setText(":  [" + index + "]");
        res_id_label.setText(":  [" + id + "]");
        res_dt_label.setText(":  [" + departure_time + "]");
        res_td_label.setText(":  [" + travel_duration + " minutes]");
    }
    
    private void arrangeSeats(String seat_string) {
        boolean[] seats_bool = new boolean[30];
        seats_bool = getAvailableSeats(seat_string);
        
        populateSeatButtons();
        
        for(int i = 0; i < seats_bool.length; i++){ 
            if(!seats_bool[i]) seat_buttons[i].setEnabled(false);
            else seat_buttons[i].setEnabled(true);
        }
    }
    
    private void seatClicked(Component selected_seat_button, String seat_string) {
        if(((javax.swing.JToggleButton) selected_seat_button).isSelected()) {
            for(Component button: seat_buttons) {
                if(selected_seat_button != button) button.setEnabled(false);
            }
        } else {
            for(Component button: seat_buttons) button.setEnabled(true);
            arrangeSeats(seat_string);
        }
    }
    
    private void populateSeatButtons() {
        seat_buttons = new Component[30];
        seat_buttons[0] = jToggleButton1;
        seat_buttons[1] = jToggleButton2;
        seat_buttons[2] = jToggleButton3;
        seat_buttons[3] = jToggleButton4;
        seat_buttons[4] = jToggleButton5;
        seat_buttons[5] = jToggleButton6;
        seat_buttons[6] = jToggleButton7;
        seat_buttons[7] = jToggleButton8;
        seat_buttons[8] = jToggleButton9;
        seat_buttons[9] = jToggleButton10;
        seat_buttons[10] = jToggleButton11;
        seat_buttons[11] = jToggleButton12;
        seat_buttons[12] = jToggleButton13;
        seat_buttons[13] = jToggleButton14;
        seat_buttons[14] = jToggleButton15;
        seat_buttons[15] = jToggleButton16;
        seat_buttons[16] = jToggleButton17;
        seat_buttons[17] = jToggleButton18;
        seat_buttons[18] = jToggleButton19;
        seat_buttons[19] = jToggleButton20;
        seat_buttons[20] = jToggleButton21;
        seat_buttons[21] = jToggleButton22;
        seat_buttons[22] = jToggleButton23;
        seat_buttons[23] = jToggleButton24;
        seat_buttons[24] = jToggleButton25;
        seat_buttons[25] = jToggleButton26;
        seat_buttons[26] = jToggleButton27;
        seat_buttons[27] = jToggleButton28;
        seat_buttons[28] = jToggleButton29;
        seat_buttons[29] = jToggleButton30;
    }
    
    private int getSelectedSeatNumber() {
        int ssn = -1;
        
        for(int i = 0; i < seat_buttons.length; i++) 
            if(((javax.swing.JToggleButton) seat_buttons[i]).isSelected()) 
                ssn = i + 1;
        
        return ssn;
    }
    
    // ------------------------ [ R E S E R V A T I O N ] ------------------------ //
    
    // Refreshing data in Bus_Details_Table
    private boolean refreshBusTable() {
        boolean result = false;
        
        try {
                connection = connect();
                
                ps = connection.prepareStatement(SELECT_ALL_BUS);
                rs = ps.executeQuery();
                
                if(rs.next()){ 
                    get_data_button.setText("Refresh Data");
                    
                    Vector<String> columnNames = getColumnNames(bus_details_table.getModel());
                    
                    bus_details_table.setModel(buildTableModel(rs, columnNames));
                    
                    result = true;
                } else { 
                    JOptionPane.showMessageDialog(rootPane, "Unknown error occured!", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
                disconnect();
                
            } catch (SQLException ex) {
                Logger.getLogger(BusRes.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(rootPane, "Error occured!\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
                bm_clear();
                st_clear();
            }
        
        return result;
    }
    
    
    // Deactivate Elements [BUS MANAGEMENT]
    private void bm_set_enabled(boolean enabled) {
        bus_id_field.setEnabled(enabled);
        bus_name_field.setEnabled(enabled);
        from_combobox.setEnabled(enabled);
        to_combobox.setEnabled(enabled);
        departure_date_field_day.setEnabled(enabled);
        departure_date_field_month.setEnabled(enabled);
        departure_date_field_year.setEnabled(enabled);
        departure_time_field_hour.setEnabled(enabled);
        departure_time_field_minute.setEnabled(enabled);
        travel_duration_field.setEnabled(enabled);
        fare_field.setEnabled(enabled);
        add_bus_button.setEnabled(enabled);
        remove_bus_button.setEnabled(enabled);
        get_data_button.setEnabled(enabled);
        remove_checkbox.setEnabled(enabled);
        bus_id_label.setEnabled(enabled);
        bus_name_label.setEnabled(enabled);
        from_label.setEnabled(enabled);
        to_label.setEnabled(enabled);
        dt_label.setEnabled(enabled);
        dd_label.setEnabled(enabled);
        td_label.setEnabled(enabled);
        tf_label.setEnabled(enabled);
        dollar_label.setEnabled(enabled);
        day_label.setEnabled(enabled);
        month_label.setEnabled(enabled);
        year_label.setEnabled(enabled);
        dot_label.setEnabled(enabled);
        minutes_label.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        res_from_combobox = new javax.swing.JComboBox<>(cities);
        jLabel3 = new javax.swing.JLabel();
        res_to_combobox = new javax.swing.JComboBox<>(cities);
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        res_departure_day = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        res_departure_month = new javax.swing.JTextField();
        res_departure_year = new javax.swing.JTextField();
        bus_list_panel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        bus_list_combobox = new javax.swing.JComboBox<>();
        jButton3 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        bus_panel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        seat_panel = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jToggleButton7 = new javax.swing.JToggleButton();
        jToggleButton8 = new javax.swing.JToggleButton();
        jToggleButton9 = new javax.swing.JToggleButton();
        jToggleButton10 = new javax.swing.JToggleButton();
        jToggleButton11 = new javax.swing.JToggleButton();
        jToggleButton12 = new javax.swing.JToggleButton();
        jToggleButton13 = new javax.swing.JToggleButton();
        jToggleButton14 = new javax.swing.JToggleButton();
        jToggleButton15 = new javax.swing.JToggleButton();
        jToggleButton16 = new javax.swing.JToggleButton();
        jToggleButton17 = new javax.swing.JToggleButton();
        jToggleButton18 = new javax.swing.JToggleButton();
        jToggleButton19 = new javax.swing.JToggleButton();
        jToggleButton20 = new javax.swing.JToggleButton();
        jToggleButton21 = new javax.swing.JToggleButton();
        jToggleButton22 = new javax.swing.JToggleButton();
        jToggleButton23 = new javax.swing.JToggleButton();
        jToggleButton24 = new javax.swing.JToggleButton();
        jToggleButton25 = new javax.swing.JToggleButton();
        jToggleButton26 = new javax.swing.JToggleButton();
        jToggleButton27 = new javax.swing.JToggleButton();
        jToggleButton28 = new javax.swing.JToggleButton();
        jToggleButton29 = new javax.swing.JToggleButton();
        jToggleButton30 = new javax.swing.JToggleButton();
        jButton40 = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        res_index_label = new javax.swing.JLabel();
        res_id_label = new javax.swing.JLabel();
        res_dt_label = new javax.swing.JLabel();
        res_td_label = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        bus_id_label = new javax.swing.JLabel();
        bus_name_label = new javax.swing.JLabel();
        from_label = new javax.swing.JLabel();
        to_label = new javax.swing.JLabel();
        dd_label = new javax.swing.JLabel();
        dt_label = new javax.swing.JLabel();
        bus_id_field = new javax.swing.JTextField();
        bus_name_field = new javax.swing.JTextField();
        from_combobox = new javax.swing.JComboBox<>(cities);
        to_combobox = new javax.swing.JComboBox<>(cities);
        td_label = new javax.swing.JLabel();
        tf_label = new javax.swing.JLabel();
        day_label = new javax.swing.JLabel();
        departure_date_field_day = new javax.swing.JTextField();
        departure_date_field_month = new javax.swing.JTextField();
        month_label = new javax.swing.JLabel();
        year_label = new javax.swing.JLabel();
        departure_date_field_year = new javax.swing.JTextField();
        departure_time_field_hour = new javax.swing.JTextField();
        dot_label = new javax.swing.JLabel();
        departure_time_field_minute = new javax.swing.JTextField();
        travel_duration_field = new javax.swing.JTextField();
        minutes_label = new javax.swing.JLabel();
        fare_field = new javax.swing.JTextField();
        dollar_label = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        bus_details_table = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        add_bus_button = new javax.swing.JButton();
        remove_bus_button = new javax.swing.JButton();
        get_data_button = new javax.swing.JButton();
        remove_checkbox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jButton11 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        ticket_details_table = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(800, 600));
        setPreferredSize(new java.awt.Dimension(800, 600));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Avant Garde", 1, 48)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Rzaaeeff's Bus Service\n");
        jLabel1.setMaximumSize(new java.awt.Dimension(800, 100));
        jLabel1.setMinimumSize(new java.awt.Dimension(800, 100));
        jLabel1.setPreferredSize(new java.awt.Dimension(800, 50));

        jTabbedPane1.setName(""); // NOI18N

        jLabel2.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        jLabel2.setText("From  :");

        jLabel3.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        jLabel3.setText("To  :");

        jLabel4.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        jLabel4.setText("Date  :");

        jLabel5.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        jLabel5.setText("DD ");

        jLabel6.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        jLabel6.setText("/ MM ");

        jLabel7.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        jLabel7.setText("/ YYYY");

        bus_list_panel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel9.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        jLabel9.setText("Avaliable Bus List  :");

        jButton3.setText("Load");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bus_list_panelLayout = new javax.swing.GroupLayout(bus_list_panel);
        bus_list_panel.setLayout(bus_list_panelLayout);
        bus_list_panelLayout.setHorizontalGroup(
            bus_list_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bus_list_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bus_list_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        bus_list_panelLayout.setVerticalGroup(
            bus_list_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bus_list_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bus_list_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(bus_list_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bus_list_panel.setVisible(false);

        jButton1.setText("Get Details");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Reset");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        bus_panel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        bus_panel.setMaximumSize(new java.awt.Dimension(552, 335));
        bus_panel.setMinimumSize(new java.awt.Dimension(552, 335));
        bus_panel.setPreferredSize(new java.awt.Dimension(552, 335));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("You have selected ");

        seat_panel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        seat_panel.setMaximumSize(new java.awt.Dimension(528, 201));
        seat_panel.setMinimumSize(new java.awt.Dimension(528, 201));
        seat_panel.setName(""); // NOI18N

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton1.setText("1");
        jToggleButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton1.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton1.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton1.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton2.setText("2");
        jToggleButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton2.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton2.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton2.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton2.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        jToggleButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton3.setText("3");
        jToggleButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton3.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton3.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton3.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton3.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        jToggleButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton4.setText("4");
        jToggleButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton4.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton4.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton4.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton4.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });

        jToggleButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton5.setText("5");
        jToggleButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton5.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton5.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton5.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton5.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton5ActionPerformed(evt);
            }
        });

        jToggleButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton6.setText("6");
        jToggleButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton6.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton6.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton6.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton6.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton6ActionPerformed(evt);
            }
        });

        jToggleButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton7.setText("7");
        jToggleButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton7.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton7.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton7.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton7.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton7ActionPerformed(evt);
            }
        });

        jToggleButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton8.setText("8");
        jToggleButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton8.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton8.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton8.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton8.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton8ActionPerformed(evt);
            }
        });

        jToggleButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton9.setText("9");
        jToggleButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton9.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton9.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton9.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton9.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton9ActionPerformed(evt);
            }
        });

        jToggleButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton10.setText("10");
        jToggleButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton10.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton10.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton10.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton10.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton10ActionPerformed(evt);
            }
        });

        jToggleButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton11.setText("11");
        jToggleButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton11.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton11.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton11.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton11.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton11ActionPerformed(evt);
            }
        });

        jToggleButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton12.setText("12");
        jToggleButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton12.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton12.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton12.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton12.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton12ActionPerformed(evt);
            }
        });

        jToggleButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton13.setText("13");
        jToggleButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton13.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton13.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton13.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton13.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton13ActionPerformed(evt);
            }
        });

        jToggleButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton14.setText("14");
        jToggleButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton14.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton14.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton14.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton14.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton14ActionPerformed(evt);
            }
        });

        jToggleButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton15.setText("15");
        jToggleButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton15.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton15.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton15.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton15.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton15ActionPerformed(evt);
            }
        });

        jToggleButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton16.setText("16");
        jToggleButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton16.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton16.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton16.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton16.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton16ActionPerformed(evt);
            }
        });

        jToggleButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton17.setText("17");
        jToggleButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton17.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton17.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton17.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton17.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton17ActionPerformed(evt);
            }
        });

        jToggleButton18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton18.setText("18");
        jToggleButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton18.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton18.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton18.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton18.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton18ActionPerformed(evt);
            }
        });

        jToggleButton19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton19.setText("19");
        jToggleButton19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton19.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton19.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton19.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton19.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton19ActionPerformed(evt);
            }
        });

        jToggleButton20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton20.setText("20");
        jToggleButton20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton20.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton20.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton20.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton20.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton20ActionPerformed(evt);
            }
        });

        jToggleButton21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton21.setText("21");
        jToggleButton21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton21.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton21.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton21.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton21.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton21ActionPerformed(evt);
            }
        });

        jToggleButton22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton22.setText("22");
        jToggleButton22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton22.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton22.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton22.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton22.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton22ActionPerformed(evt);
            }
        });

        jToggleButton23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton23.setText("23");
        jToggleButton23.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton23.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton23.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton23.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton23.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton23ActionPerformed(evt);
            }
        });

        jToggleButton24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton24.setText("24");
        jToggleButton24.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton24.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton24.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton24.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton24.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton24ActionPerformed(evt);
            }
        });

        jToggleButton25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton25.setText("25");
        jToggleButton25.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton25.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton25.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton25.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton25.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton25ActionPerformed(evt);
            }
        });

        jToggleButton26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton26.setText("26");
        jToggleButton26.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton26.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton26.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton26.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton26.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton26ActionPerformed(evt);
            }
        });

        jToggleButton27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton27.setText("27");
        jToggleButton27.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton27.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton27.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton27.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton27.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton27ActionPerformed(evt);
            }
        });

        jToggleButton28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton28.setText("28");
        jToggleButton28.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton28.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton28.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton28.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton28.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton28ActionPerformed(evt);
            }
        });

        jToggleButton29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton29.setText("29");
        jToggleButton29.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton29.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton29.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton29.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton29.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton29ActionPerformed(evt);
            }
        });

        jToggleButton30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_av_32.png"))); // NOI18N
        jToggleButton30.setText("30");
        jToggleButton30.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton30.setMaximumSize(new java.awt.Dimension(45, 45));
        jToggleButton30.setMinimumSize(new java.awt.Dimension(45, 45));
        jToggleButton30.setPreferredSize(new java.awt.Dimension(45, 45));
        jToggleButton30.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/busres/resources/seat_un_32.png"))); // NOI18N
        jToggleButton30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton30ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout seat_panelLayout = new javax.swing.GroupLayout(seat_panel);
        seat_panel.setLayout(seat_panelLayout);
        seat_panelLayout.setHorizontalGroup(
            seat_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(seat_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(seat_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(seat_panelLayout.createSequentialGroup()
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jToggleButton19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(seat_panelLayout.createSequentialGroup()
                        .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jToggleButton20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(seat_panelLayout.createSequentialGroup()
                        .addComponent(jToggleButton21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jToggleButton30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        seat_panelLayout.setVerticalGroup(
            seat_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(seat_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(seat_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(seat_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(seat_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jButton40.setText("Make Reservation");
        jButton40.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton40ActionPerformed(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("Bus ID              ");

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel28.setText("Travel Duration      ");

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel29.setText("Departure Time   ");

        res_index_label.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        res_index_label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        res_index_label.setText(":  [17]");

        res_id_label.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        res_id_label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        res_id_label.setText(":  [103]");

        res_dt_label.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        res_dt_label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        res_dt_label.setText(":  [14:50]");

        res_td_label.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        res_td_label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        res_td_label.setText(":  [45 minutes]");

        javax.swing.GroupLayout bus_panelLayout = new javax.swing.GroupLayout(bus_panel);
        bus_panel.setLayout(bus_panelLayout);
        bus_panelLayout.setHorizontalGroup(
            bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bus_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(bus_panelLayout.createSequentialGroup()
                        .addComponent(seat_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(bus_panelLayout.createSequentialGroup()
                        .addGroup(bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel28, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(bus_panelLayout.createSequentialGroup()
                                .addComponent(res_index_label, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                .addGap(496, 496, 496))
                            .addComponent(res_id_label, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                            .addComponent(res_td_label, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                            .addComponent(res_dt_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        bus_panelLayout.setVerticalGroup(
            bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bus_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(res_index_label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(res_id_label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(res_dt_label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(res_td_label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(bus_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton40, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addComponent(seat_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        bus_panel.setVisible(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(bus_panel, javax.swing.GroupLayout.DEFAULT_SIZE, 766, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(bus_list_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(res_from_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(res_to_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(res_departure_day, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(res_departure_month, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(res_departure_year, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(19, 19, 19))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(res_from_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(res_to_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(res_departure_year, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(res_departure_month, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(res_departure_day, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bus_list_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bus_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Reservation", jPanel1);

        bus_id_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        bus_id_label.setText("Bus ID            :");

        bus_name_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        bus_name_label.setText("Bus Name  :");

        from_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        from_label.setText("From             :");

        to_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        to_label.setText("To                   :");

        dd_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        dd_label.setText("Departure Date :");

        dt_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        dt_label.setText("Departure Time :");

        td_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        td_label.setText("Travel Duration :");

        tf_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        tf_label.setText("Travel Fare          :");

        day_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        day_label.setText("DD ");

        month_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        month_label.setText("/ MM ");

        year_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        year_label.setText("/ YYYY");

        dot_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        dot_label.setText(":");

        minutes_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        minutes_label.setText("minutes");

        dollar_label.setFont(new java.awt.Font("Avant Garde", 1, 14)); // NOI18N
        dollar_label.setText("$");

        jScrollPane1.setMaximumSize(new java.awt.Dimension(760, 290));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(760, 290));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(760, 290));

        bus_details_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Bus ID", "Bus Name", "From", "To", "Departure Time", "Departure Date", "Travel Duration", "Travel Fare"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        bus_details_table.setEnabled(false);
        jScrollPane1.setViewportView(bus_details_table);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setPreferredSize(new java.awt.Dimension(4, 110));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        add_bus_button.setText("Add Bus");
        add_bus_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_bus_buttonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 60;
        jPanel4.add(add_bus_button, gridBagConstraints);

        remove_bus_button.setText("Remove Bus");
        remove_bus_button.setEnabled(false);
        remove_bus_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remove_bus_buttonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 50;
        jPanel4.add(remove_bus_button, gridBagConstraints);

        get_data_button.setText("Get Data");
        get_data_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                get_data_buttonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel4.add(get_data_button, gridBagConstraints);

        remove_checkbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remove_checkboxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel4.add(remove_checkbox, gridBagConstraints);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                        .addComponent(to_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(to_combobox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                        .addComponent(from_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(from_combobox, 0, 149, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                        .addComponent(bus_name_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bus_name_field)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(tf_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(dollar_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fare_field))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                        .addComponent(td_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(travel_duration_field))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(dd_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(day_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(departure_date_field_day, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(month_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(departure_date_field_month, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(year_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(departure_date_field_year, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(minutes_label)))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(bus_id_label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bus_id_field, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(dt_label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(departure_time_field_hour, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dot_label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(departure_time_field_minute, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(25, 25, 25))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGap(44, 44, 44))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bus_id_label)
                            .addComponent(dt_label)
                            .addComponent(bus_id_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(departure_time_field_hour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dot_label)
                            .addComponent(departure_time_field_minute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(day_label)
                                    .addComponent(departure_date_field_day, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(month_label)
                                    .addComponent(departure_date_field_month, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(year_label)
                                    .addComponent(departure_date_field_year, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(bus_name_label)
                                    .addComponent(bus_name_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dd_label))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(from_label)
                            .addComponent(from_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(td_label)
                            .addComponent(travel_duration_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minutes_label))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(to_label)
                            .addComponent(to_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tf_label)
                            .addComponent(dollar_label)
                            .addComponent(fare_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Bus Management", jPanel3);

        jButton11.setText("Get Data");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jScrollPane2.setMaximumSize(new java.awt.Dimension(770, 390));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(770, 390));
        jScrollPane2.setName(""); // NOI18N
        jScrollPane2.setPreferredSize(new java.awt.Dimension(770, 390));

        ticket_details_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Ticket ID", "Bus ID", "Seat Number", "Customer Name", "Customer Surname", "Customer Phone Number"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(ticket_details_table);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 770, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jButton11)
                .addGap(7, 7, 7)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Tickets", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1))
        );

        jLabel1.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int index = bus_list_combobox.getSelectedIndex();
        int id = bus_ids.get(index);
        res_id = id;
        String dtime, tduration;
        selected_seat_number = -1;
        
        try {
                connection = connect();
                
                ps = connection.prepareStatement(SELECT_BUS);
                ps.setInt(1, id);
                
                rs = ps.executeQuery();
                
                if(rs.next()){ 
                    seats = "";
                    dtime = rs.getString("DEPARTURE_TIME");
                    tduration = rs.getString("TRAVEL_DURATION");
                    seats = rs.getString("SEATS");
                    
                    populateBusPanel(Integer.toString(index + 1), Integer.toString(id), dtime, tduration);
                    arrangeSeats(seats);
                    bus_panel.setVisible(true);
                } else { 
                    JOptionPane.showMessageDialog(rootPane, "Unknown error occured!", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
                disconnect();
                
            } catch (SQLException ex) {
                Logger.getLogger(BusRes.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(rootPane, "Error occured!\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
                bm_clear();
                st_clear();
            }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        st_clear();
        res_getBusDetails();
        
        // ------------------------ [ C H E C K I N G ] ------------------------ //
        if(st_fm.equals(cities[0])) JOptionPane.showMessageDialog(rootPane, "Please choose city.\nFrom which city?", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(st_to.equals(cities[0])) JOptionPane.showMessageDialog(rootPane, "Please choose city.\nTo which city?", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(st_fm.equals(st_to)) JOptionPane.showMessageDialog(rootPane, "Source and Destination cities cannot be the same.", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(!isDateValid(st_dd) || res_departure_year.getText().length() < 4) JOptionPane.showMessageDialog(rootPane, "Entered date is invalid.\n"
                + "Format is: dd/MM/yyyy\nFor example: 31/03/2003", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(res_departure_day.getText().length() < 2 || res_departure_month.getText().length() < 2) { 
            if(res_departure_day.getText().length() < 2) res_departure_day.setText("0" + res_departure_day.getText());
            if(res_departure_month.getText().length() < 2) res_departure_month.setText("0" + res_departure_month.getText());
            jButton1ActionPerformed(evt);
        }
        // ------------------------ [ C H E C K I N G ] ------------------------ //
        
        else {
            try {
                connection = connect();
                
                ps = connection.prepareStatement(SELECT_BUSES_RESERVATION);
                ps.setString(1, st_fm);
                ps.setString(2, st_to);
                ps.setString(3, st_dd);
                rs = ps.executeQuery();
                
                if(rs.next()){
                    int count = 0;
                    box_data = new ArrayList<String>();
                    bus_ids = new ArrayList<Integer>();
                    
                    do {
                        bus_ids.add(rs.getInt("BUS_ID"));
                        box_data.add("[" + (count + 1) + "] " + bus_ids.get(count)
                                + " from [" + st_fm + "] to [" + st_to + "]"); 
                        count++;
                    } while(rs.next());
                    bus_list_combobox.setModel(new DefaultComboBoxModel(box_data.toArray()));
                    
                    canReset = true;
                    bus_list_panel.setVisible(true);
                    jButton1.setVisible(false);
                    
                } else {
                    JOptionPane.showMessageDialog(rootPane, "There is no such bus.\n"
                            + "Please, choose different credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
                disconnect();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(rootPane, "Error occured!\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
                res_clear();
                st_clear();
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if(canReset) {
            if (JOptionPane.showConfirmDialog(null, "Are you sure?", "WARNING",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION && canReset) {
                res_clear();
                bus_panel.setVisible(false);
                bus_list_panel.setVisible(false);
                canReset = false;
                jButton1.setVisible(true);
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        this.setLocationRelativeTo(null); // centering
        
        bm_set_enabled(false);
        get_data_button.setEnabled(true);
        
        try {
            System.out.println("Testing connectivity...");
            connection = connect();
            System.out.println("\nClosing connection...");
            disconnect();
            System.out.println("[OK] Connected without any problem!\n"
                    + "[OK] Disconnected without any problem!");
        } catch (SQLException exc) {
            JOptionPane.showMessageDialog(rootPane, "Error occured!\nClosing application.\n"
                    + "Error: " + exc.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_formWindowOpened

    private void get_data_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_get_data_buttonActionPerformed
        if(refreshBusTable()) {
            bm_set_enabled(true);
            remove_bus_button.setEnabled(false);
        }
    }//GEN-LAST:event_get_data_buttonActionPerformed

    private void remove_checkboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remove_checkboxActionPerformed
        if(remove_checkbox.isSelected()) {
            bm_set_enabled(false);
            bus_id_label.setEnabled(true);
            bus_id_field.setEnabled(true);
            remove_bus_button.setEnabled(true);
            remove_checkbox.setEnabled(true);
        } else {
            bm_set_enabled(true);
            remove_bus_button.setEnabled(false);
        }
    }//GEN-LAST:event_remove_checkboxActionPerformed

    private void remove_bus_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remove_bus_buttonActionPerformed
        st_id = bus_id_field.getText();
        if(!isNumeric(st_id)) JOptionPane.showMessageDialog(rootPane, "Entered bus ID is invalid!\n"
                + "Please, enter numeric values only.", "Error!", JOptionPane.ERROR_MESSAGE);
        else {
            try {
                connection = connect();
                
                ps = connection.prepareStatement(DELETE);
                ps.setInt(1, Integer.parseInt(st_id));
                
                int success = ps.executeUpdate();
                
                if(success > 0){ 
                    JOptionPane.showMessageDialog(rootPane, "Bus deleted!", "Information", JOptionPane.INFORMATION_MESSAGE);
                    bus_id_field.setText("");
                    st_id = "";
                    refreshBusTable();
                    
                } else JOptionPane.showMessageDialog(rootPane, "Unknown error occured!", "Error", JOptionPane.ERROR_MESSAGE);
                
                disconnect();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(rootPane, "Error occured!\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
                bus_id_field.setText("");
                st_id = "";
            }
        }
        
    }//GEN-LAST:event_remove_bus_buttonActionPerformed

    private void add_bus_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_bus_buttonActionPerformed
        st_clear();
        bm_getBusDetails();
        
        // ------------------------ [ C H E C K I N G ] ------------------------ //
        if(st_fm.equals(cities[0])) JOptionPane.showMessageDialog(rootPane, "Please choose city.\nFrom which city?", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(st_to.equals(cities[0])) JOptionPane.showMessageDialog(rootPane, "Please choose city.\nTo which city?", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(st_fm.equals(st_to)) JOptionPane.showMessageDialog(rootPane, "Source and Destination cities cannot be the same.", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(bm_isAnyEmpty()) JOptionPane.showMessageDialog(rootPane, "Please, fill all fields.", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(!isTimeValid(st_dt)) JOptionPane.showMessageDialog(rootPane, "Entered time is invalid.\n"
                + "Format is: HH:mm\nFor example: 14:50", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(!isDateValid(st_dd) || departure_date_field_year.getText().length() < 4) 
            JOptionPane.showMessageDialog(rootPane, "Entered date is invalid.\n"
                + "Format is: dd/MM/yyyy\nFor example: 31/03/2003", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(departure_date_field_day.getText().length() < 2 || departure_date_field_month.getText().length() < 2){ 
            if(departure_date_field_day.getText().length() < 2) departure_date_field_day.setText("0" + departure_date_field_day.getText());
            if(departure_date_field_month.getText().length() < 2) departure_date_field_month.setText("0" + departure_date_field_month.getText());
            add_bus_buttonActionPerformed(evt);
        } else if(departure_time_field_hour.getText().length() < 2 || departure_time_field_minute.getText().length() < 2) {
            if(departure_time_field_hour.getText().length() < 2) 
                departure_time_field_hour.setText("0" + departure_time_field_hour.getText());
            if(departure_time_field_minute.getText().length() < 2)
                departure_time_field_minute.setText("0" + departure_time_field_minute.getText());
            add_bus_buttonActionPerformed(evt);
        } else if(!isNumeric(st_id)) JOptionPane.showMessageDialog(rootPane, "Entered bus ID is invalid!\n"
                + "Please, enter numeric values only.", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(!isNumeric(st_td)) JOptionPane.showMessageDialog(rootPane, "Entered travel duration is invalid!\n"
                + "Please, enter numeric values only.", "Error!", JOptionPane.ERROR_MESSAGE);
        else if(!isNumeric(st_tf)) JOptionPane.showMessageDialog(rootPane, "Entered travel fare is invalid.\n"
                + "Please, enter numeric values only.", "Error!", JOptionPane.ERROR_MESSAGE);
        // ------------------------ [ C H E C K I N G ] ------------------------ //
        
        else {
            try {
                connection = connect();
                
                ps = connection.prepareStatement(INSERT);
                ps.setInt(1, Integer.parseInt(st_id));
                ps.setString(2, st_nm);
                ps.setString(3, st_fm);
                ps.setString(4, st_to);
                ps.setString(5, st_dt);
                ps.setString(6, st_dd);
                ps.setInt(7, Integer.parseInt(st_td));
                ps.setInt(8, Integer.parseInt(st_tf));
                
                int success = ps.executeUpdate();
                
                if(success > 0){ 
                    JOptionPane.showMessageDialog(rootPane, "Bus added!", "Information", JOptionPane.INFORMATION_MESSAGE);
                    bm_clear();
                    st_clear();
                    refreshBusTable();
                } else JOptionPane.showMessageDialog(rootPane, "Unknown error occured!", "Error", JOptionPane.ERROR_MESSAGE);
                
                disconnect();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(rootPane, "Error occured!\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
                bm_clear();
                st_clear();
            }
        }
    }//GEN-LAST:event_add_bus_buttonActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        try {
                connection = connect();
                
                ps = connection.prepareStatement(SELECT_ALL_TICKET);
                rs = ps.executeQuery();
                
                if(rs.next()){ 
                    jButton11.setText("Refresh Data");
                    
                    Vector<String> columnNames = getColumnNames(ticket_details_table.getModel());
                    
                    ticket_details_table.setModel(buildTableModel(rs, columnNames));
                    
                } else { 
                    JOptionPane.showMessageDialog(rootPane, "Unknown error occured!", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
                disconnect();
                
            } catch (SQLException ex) {
                Logger.getLogger(BusRes.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(rootPane, "Error occured!\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton5ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton5ActionPerformed

    private void jToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton7ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton7ActionPerformed

    private void jToggleButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton9ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton9ActionPerformed

    private void jToggleButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton11ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton11ActionPerformed

    private void jToggleButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton13ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton13ActionPerformed

    private void jToggleButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton15ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton15ActionPerformed

    private void jToggleButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton17ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton17ActionPerformed

    private void jToggleButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton19ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton19ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    private void jToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton6ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton6ActionPerformed

    private void jToggleButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton8ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton8ActionPerformed

    private void jToggleButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton10ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton10ActionPerformed

    private void jToggleButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton12ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton12ActionPerformed

    private void jToggleButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton14ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton14ActionPerformed

    private void jToggleButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton16ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton16ActionPerformed

    private void jToggleButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton18ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton18ActionPerformed

    private void jToggleButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton20ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton20ActionPerformed

    private void jToggleButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton21ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton21ActionPerformed

    private void jToggleButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton22ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton22ActionPerformed

    private void jToggleButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton23ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton23ActionPerformed

    private void jToggleButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton24ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton24ActionPerformed

    private void jToggleButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton25ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton25ActionPerformed

    private void jToggleButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton26ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton26ActionPerformed

    private void jToggleButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton27ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton27ActionPerformed

    private void jToggleButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton28ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton28ActionPerformed

    private void jToggleButton29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton29ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton29ActionPerformed

    private void jToggleButton30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton30ActionPerformed
        selected_seat_button = (Component) evt.getSource();
        seatClicked(selected_seat_button, seats);
    }//GEN-LAST:event_jToggleButton30ActionPerformed

    private void jButton40ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton40ActionPerformed
        selected_seat_number = getSelectedSeatNumber();
        
        if(selected_seat_number == -1) 
            JOptionPane.showMessageDialog(rootPane, "Please, choose seat.", "Error!", JOptionPane.ERROR_MESSAGE);
        else {
            javax.swing.JDialog details = new Details(this, true);
            details.setVisible(true);
            
            if(isDialogDone) {
                try {
                    connection = connect();

                    String seats_new = reserveSeat(seats, (byte) selected_seat_number);

                    ps = connection.prepareStatement(INSERT_SEAT);
                    ps.setString(1, seats_new);
                    ps.setInt(2, res_id);

                    int first_step = ps.executeUpdate();

                    if(first_step > 0){ 
                        ps = connection.prepareStatement(INSERT_INTO_TICKETS);
                        ps.setInt(1, res_id);
                        ps.setInt(2, selected_seat_number);
                        ps.setString(3, customer_data[0]);
                        ps.setString(4, customer_data[1]);
                        ps.setString(5, customer_data[2]);

                        int second_step = ps.executeUpdate();

                        if(second_step > 0) {
                            JOptionPane.showMessageDialog(rootPane, "Reserved!", "Information", JOptionPane.INFORMATION_MESSAGE);
                            arrangeSeats(seats_new);

                        } else {
                            JOptionPane.showMessageDialog(rootPane, "Unknown error occured!", "Error!", JOptionPane.ERROR_MESSAGE);

                            ps = connection.prepareStatement(INSERT_SEAT);
                            ps.setString(1, seats_new);
                            ps.setInt(2, res_id);

                            ps.executeUpdate();
                        }

                    }

                    disconnect();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(rootPane, "Error occured!\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_jButton40ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BusRes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BusRes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BusRes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BusRes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BusRes().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add_bus_button;
    private javax.swing.JTable bus_details_table;
    private javax.swing.JTextField bus_id_field;
    private javax.swing.JLabel bus_id_label;
    private javax.swing.JComboBox<String> bus_list_combobox;
    private javax.swing.JPanel bus_list_panel;
    private javax.swing.JTextField bus_name_field;
    private javax.swing.JLabel bus_name_label;
    private javax.swing.JPanel bus_panel;
    private javax.swing.JLabel day_label;
    private javax.swing.JLabel dd_label;
    private javax.swing.JTextField departure_date_field_day;
    private javax.swing.JTextField departure_date_field_month;
    private javax.swing.JTextField departure_date_field_year;
    private javax.swing.JTextField departure_time_field_hour;
    private javax.swing.JTextField departure_time_field_minute;
    private javax.swing.JLabel dollar_label;
    private javax.swing.JLabel dot_label;
    private javax.swing.JLabel dt_label;
    private javax.swing.JTextField fare_field;
    private javax.swing.JComboBox<String> from_combobox;
    private javax.swing.JLabel from_label;
    private javax.swing.JButton get_data_button;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton40;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton10;
    private javax.swing.JToggleButton jToggleButton11;
    private javax.swing.JToggleButton jToggleButton12;
    private javax.swing.JToggleButton jToggleButton13;
    private javax.swing.JToggleButton jToggleButton14;
    private javax.swing.JToggleButton jToggleButton15;
    private javax.swing.JToggleButton jToggleButton16;
    private javax.swing.JToggleButton jToggleButton17;
    private javax.swing.JToggleButton jToggleButton18;
    private javax.swing.JToggleButton jToggleButton19;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton20;
    private javax.swing.JToggleButton jToggleButton21;
    private javax.swing.JToggleButton jToggleButton22;
    private javax.swing.JToggleButton jToggleButton23;
    private javax.swing.JToggleButton jToggleButton24;
    private javax.swing.JToggleButton jToggleButton25;
    private javax.swing.JToggleButton jToggleButton26;
    private javax.swing.JToggleButton jToggleButton27;
    private javax.swing.JToggleButton jToggleButton28;
    private javax.swing.JToggleButton jToggleButton29;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton30;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JToggleButton jToggleButton8;
    private javax.swing.JToggleButton jToggleButton9;
    private javax.swing.JLabel minutes_label;
    private javax.swing.JLabel month_label;
    private javax.swing.JButton remove_bus_button;
    private javax.swing.JCheckBox remove_checkbox;
    private javax.swing.JTextField res_departure_day;
    private javax.swing.JTextField res_departure_month;
    private javax.swing.JTextField res_departure_year;
    private javax.swing.JLabel res_dt_label;
    private javax.swing.JComboBox<String> res_from_combobox;
    private javax.swing.JLabel res_id_label;
    private javax.swing.JLabel res_index_label;
    private javax.swing.JLabel res_td_label;
    private javax.swing.JComboBox<String> res_to_combobox;
    private javax.swing.JPanel seat_panel;
    private javax.swing.JLabel td_label;
    private javax.swing.JLabel tf_label;
    private javax.swing.JTable ticket_details_table;
    private javax.swing.JComboBox<String> to_combobox;
    private javax.swing.JLabel to_label;
    private javax.swing.JTextField travel_duration_field;
    private javax.swing.JLabel year_label;
    // End of variables declaration//GEN-END:variables

    // BEGIN    [Seats]
    private Component[] seat_buttons;
    private Component selected_seat_button;
    private int selected_seat_number;
    // END      [Seats]
    
}
