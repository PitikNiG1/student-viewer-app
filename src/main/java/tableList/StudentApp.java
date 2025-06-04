package tableList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.FlatLightLaf;

// Imports for GTable and its components
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import net.miginfocom.swing.MigLayout;


/**
 * Student POJO Class
 * Represents a student with their details.
 */
class Student {
    private String lrn;
    private String lastName;
    private String firstName;
    private String middleName;
    private String sex;
    private int age;
    private String gradeLevel;
    private String section;
    private String trackAndStrand;

    public Student(String lrn, String lastName, String firstName, String middleName,
                   String sex, int age, String gradeLevel, String section, String trackAndStrand) {
        this.lrn = lrn;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.sex = sex;
        this.age = age;
        this.gradeLevel = gradeLevel;
        this.section = section;
        this.trackAndStrand = trackAndStrand;
    }

    // Getters
    public String getLrn() { return lrn; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName == null ? "" : middleName; }
    public String getSex() { return sex; }
    public int getAge() { return age; }
    public String getGradeLevel() { return gradeLevel; }
    public String getSection() { return section; }
    public String getTrackAndStrand() { return trackAndStrand; }

    /**
     * Gets the full name of the student (LastName, FirstName M.).
     * @return The full name.
     */
    public String getFullName() {
        return lastName + ", " + firstName + " " + (middleName != null && !middleName.isEmpty() ? middleName.charAt(0) + "." : "");
    }

     /**
     * Gets the full name of the student in format: FirstName MiddleInitial. LastName.
     * @return The formatted full name.
     */
    public String getFormattedName() {
        StringJoiner sj = new StringJoiner(" ");
        if (firstName != null && !firstName.isEmpty()) sj.add(firstName);
        if (middleName != null && !middleName.isEmpty()) sj.add(middleName.charAt(0) + ".");
        if (lastName != null && !lastName.isEmpty()) sj.add(lastName);
        return sj.toString();
    }


    @Override
    public String toString() {
        return "Student{" +
               "lrn='" + lrn + '\'' +
               ", fullName='" + getFullName() + '\'' +
               ", age=" + age +
               ", gradeLevel='" + gradeLevel + '\'' +
               '}';
    }
}

/**
 * FilterCriteria Class
 * Holds the criteria for filtering student data.
 */
class FilterCriteria {
    String searchTerm = ""; // General search for LRN or parts of name
    String filterFirstName = "";
    String filterLastName = "";
    String filterMiddleName = "";
    boolean middleInitialOnly = false;
    String filterGradeLevel = "All";
    String filterSection = "All"; // Added section filter
    String filterTrackStrand = "All";
    boolean filterMale = true;
    boolean filterFemale = true;
    int minAge = 0;
    int maxAge = 100;

    public FilterCriteria() {}

    /**
     * Resets all filter criteria to their default values.
     * @param dbMinAge The minimum age found in the database (for default setting).
     * @param dbMaxAge The maximum age found in the database (for default setting).
     */
    public void reset(int dbMinAge, int dbMaxAge) {
        searchTerm = "";
        filterFirstName = "";
        filterLastName = "";
        filterMiddleName = "";
        middleInitialOnly = false;
        filterGradeLevel = "All";
        filterSection = "All";
        filterTrackStrand = "All";
        filterMale = true;
        filterFemale = true;
        minAge = dbMinAge;
        maxAge = dbMaxAge > dbMinAge ? dbMaxAge + 5 : dbMinAge + 20; // Default max age with a small buffer
    }

    /**
     * Checks if any specific filters (beyond search term and default age/sex) are active.
     * @param dbMinAge Minimum age from DB for default comparison.
     * @param dbMaxAge Maximum age from DB for default comparison.
     * @return True if specific filters are active, false otherwise.
     */
    public boolean hasActiveSpecificFilters(int dbMinAge, int dbMaxAge) {
        if (filterFirstName != null && !filterFirstName.isEmpty()) return true;
        if (filterLastName != null && !filterLastName.isEmpty()) return true;
        if (filterMiddleName != null && !filterMiddleName.isEmpty()) return true;
        if (!"All".equals(filterGradeLevel)) return true;
        if (!"All".equals(filterSection)) return true;
        if (!"All".equals(filterTrackStrand)) return true;
        // Check if sex filter is non-default (i.e., not both true)
        if (!filterMale || !filterFemale) {
            if (filterMale != filterFemale) return true; // Only one is selected
            if (!filterMale && !filterFemale) return true; // None selected (effectively filtering all out)
        }
        // Check if age is different from default range
        int defaultMaxAge = dbMaxAge > dbMinAge ? dbMaxAge + 5 : dbMinAge + 20;
        if (minAge != dbMinAge || maxAge != defaultMaxAge) return true;

        return false;
    }

    /**
     * Counts the number of active filters.
     * @param dbMinAge Minimum age from DB for default comparison.
     * @param dbMaxAge Maximum age from DB for default comparison.
     * @return The count of active filters.
     */
    public int getActiveFilterCount(int dbMinAge, int dbMaxAge) {
        int count = 0;
        if (searchTerm != null && !searchTerm.isEmpty()) count++;
        if (filterFirstName != null && !filterFirstName.isEmpty()) count++;
        if (filterLastName != null && !filterLastName.isEmpty()) count++;
        if (filterMiddleName != null && !filterMiddleName.isEmpty()) count++;
        if (!"All".equals(filterGradeLevel)) count++;
        if (!"All".equals(filterSection)) count++;
        if (!"All".equals(filterTrackStrand)) count++;
        if (!filterMale || !filterFemale) { // If not both are true (default)
             if (filterMale != filterFemale) count++; // Only one selected counts as a filter
             else if (!filterMale && !filterFemale) count++; // Neither selected also counts
        }
        int defaultMaxAge = dbMaxAge > dbMinAge ? dbMaxAge + 5 : dbMinAge + 20;
        if (minAge != dbMinAge || maxAge != defaultMaxAge) {
            count++;
        }
        return count;
    }
}

/**
 * DatabaseManager Class
 * Handles database operations for fetching student data.
 */
class DatabaseManager {

    public DatabaseManager() {
    }

    /**
     * Fetches a list of students from the database based on filter criteria.
     *
     * @param page       The current page number (1-indexed). Ignored if pageSize is MAX_VALUE.
     * @param pageSize   The number of students per page. If Integer.MAX_VALUE, fetches all.
     * @param criteria   The filter criteria to apply.
     * @return A list of Student objects.
     */
    public List<Student> getStudents(int page, int pageSize, FilterCriteria criteria) {
        List<Student> students = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT s.STUDENT_LRN, s.STUDENT_LASTNAME, s.STUDENT_FIRSTNAME, s.STUDENT_MIDDLENAME, " +
            "s.STUDENT_SEX, s.STUDENT_AGE, sf.SF_GRADE_LEVEL, sf.SF_SECTION, sf.SF_TRACK_AND_STRAND " +
            "FROM STUDENT s JOIN SCHOOL_FORM sf ON s.SF_ID = sf.SF_ID"
        );
        StringBuilder whereClause = new StringBuilder();

        // General Search Term (LRN or Name parts)
        if (criteria.searchTerm != null && !criteria.searchTerm.isEmpty()) {
            addCondition(whereClause,
                "(s.STUDENT_LRN LIKE ? OR " +
                "LOWER(s.STUDENT_FIRSTNAME) LIKE ? OR " +
                "LOWER(s.STUDENT_LASTNAME) LIKE ? OR " +
                "LOWER(s.STUDENT_MIDDLENAME) LIKE ? OR " +
                "LOWER(CONCAT(s.STUDENT_FIRSTNAME, ' ', s.STUDENT_LASTNAME)) LIKE ? OR " +
                "LOWER(CONCAT(s.STUDENT_LASTNAME, ', ', s.STUDENT_FIRSTNAME)) LIKE ?)"
            );
            String searchTermParam = "%" + criteria.searchTerm.toLowerCase() + "%";
            for(int i=0; i<6; i++) params.add(searchTermParam);
        }

        // Detailed Name Filters (if search term is not primarily used for names)
        if (criteria.filterFirstName != null && !criteria.filterFirstName.isEmpty()) {
            addCondition(whereClause, "LOWER(s.STUDENT_FIRSTNAME) LIKE ?");
            params.add("%" + criteria.filterFirstName.toLowerCase() + "%");
        }
        if (criteria.filterLastName != null && !criteria.filterLastName.isEmpty()) {
            addCondition(whereClause, "LOWER(s.STUDENT_LASTNAME) LIKE ?");
            params.add("%" + criteria.filterLastName.toLowerCase() + "%");
        }
        if (criteria.filterMiddleName != null && !criteria.filterMiddleName.isEmpty()) {
            if (criteria.middleInitialOnly) {
                addCondition(whereClause, "LOWER(s.STUDENT_MIDDLENAME) LIKE ?");
                params.add(criteria.filterMiddleName.toLowerCase().charAt(0) + "%");
            } else {
                addCondition(whereClause, "LOWER(s.STUDENT_MIDDLENAME) LIKE ?");
                params.add("%" + criteria.filterMiddleName.toLowerCase() + "%");
            }
        }
        // Grade Level Filter
        if (!"All".equals(criteria.filterGradeLevel)) {
            addCondition(whereClause, "sf.SF_GRADE_LEVEL = ?");
            params.add(criteria.filterGradeLevel);
        }
        // Section Filter
        if (!"All".equals(criteria.filterSection)) {
            addCondition(whereClause, "sf.SF_SECTION = ?");
            params.add(criteria.filterSection);
        }
        // Track & Strand Filter
        if (!"All".equals(criteria.filterTrackStrand)) {
            addCondition(whereClause, "sf.SF_TRACK_AND_STRAND = ?");
            params.add(criteria.filterTrackStrand);
        }
        // Sex Filter
        if (!criteria.filterMale && criteria.filterFemale) {
            addCondition(whereClause, "s.STUDENT_SEX = ?"); params.add("Female");
        } else if (criteria.filterMale && !criteria.filterFemale) {
            addCondition(whereClause, "s.STUDENT_SEX = ?"); params.add("Male");
        } else if (!criteria.filterMale && !criteria.filterFemale) { // Neither selected
            addCondition(whereClause, "1 = 0"); // Effectively returns no results for sex
        }
        // Age Range Filter
        addCondition(whereClause, "s.STUDENT_AGE BETWEEN ? AND ?");
        params.add(criteria.minAge); params.add(criteria.maxAge);

        if (whereClause.length() > 0) {
            sqlBuilder.append(" WHERE ").append(whereClause);
        }

        sqlBuilder.append(" ORDER BY s.STUDENT_LASTNAME ASC, s.STUDENT_FIRSTNAME ASC");

        if (pageSize != Integer.MAX_VALUE) { // Apply pagination only if not fetching all
            sqlBuilder.append(" LIMIT ? OFFSET ?");
            params.add(pageSize);
            params.add((page - 1) * pageSize);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            // System.out.println("Executing SQL: " + pstmt.toString()); // For debugging
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                        rs.getString("STUDENT_LRN"), rs.getString("STUDENT_LASTNAME"),
                        rs.getString("STUDENT_FIRSTNAME"), rs.getString("STUDENT_MIDDLENAME"),
                        rs.getString("STUDENT_SEX"), rs.getInt("STUDENT_AGE"),
                        rs.getString("SF_GRADE_LEVEL"), rs.getString("SF_SECTION"),
                        rs.getString("SF_TRACK_AND_STRAND")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching student data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return students;
    }

    /**
     * Counts the total number of students matching the filter criteria.
     * @param criteria The filter criteria.
     * @return The total count of matching students.
     */
    public int getTotalStudentCount(FilterCriteria criteria) {
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(DISTINCT s.STUDENT_LRN) FROM STUDENT s JOIN SCHOOL_FORM sf ON s.SF_ID = sf.SF_ID");
        StringBuilder whereClause = new StringBuilder();

        // Apply the same filtering logic as in getStudents()
        if (criteria.searchTerm != null && !criteria.searchTerm.isEmpty()) {
             addCondition(whereClause,
                "(s.STUDENT_LRN LIKE ? OR " +
                "LOWER(s.STUDENT_FIRSTNAME) LIKE ? OR " +
                "LOWER(s.STUDENT_LASTNAME) LIKE ? OR " +
                "LOWER(s.STUDENT_MIDDLENAME) LIKE ? OR " +
                "LOWER(CONCAT(s.STUDENT_FIRSTNAME, ' ', s.STUDENT_LASTNAME)) LIKE ? OR " +
                "LOWER(CONCAT(s.STUDENT_LASTNAME, ', ', s.STUDENT_FIRSTNAME)) LIKE ?)"
            );
            String searchTermParam = "%" + criteria.searchTerm.toLowerCase() + "%";
            for(int i=0; i<6; i++) params.add(searchTermParam);
        }
        if (criteria.filterFirstName != null && !criteria.filterFirstName.isEmpty()) {
            addCondition(whereClause, "LOWER(s.STUDENT_FIRSTNAME) LIKE ?");
            params.add("%" + criteria.filterFirstName.toLowerCase() + "%");
        }
        if (criteria.filterLastName != null && !criteria.filterLastName.isEmpty()) {
            addCondition(whereClause, "LOWER(s.STUDENT_LASTNAME) LIKE ?");
            params.add("%" + criteria.filterLastName.toLowerCase() + "%");
        }
        if (criteria.filterMiddleName != null && !criteria.filterMiddleName.isEmpty()) {
            if (criteria.middleInitialOnly) {
                addCondition(whereClause, "LOWER(s.STUDENT_MIDDLENAME) LIKE ?");
                params.add(criteria.filterMiddleName.toLowerCase().charAt(0) + "%");
            } else {
                addCondition(whereClause, "LOWER(s.STUDENT_MIDDLENAME) LIKE ?");
                params.add("%" + criteria.filterMiddleName.toLowerCase() + "%");
            }
        }
        if (!"All".equals(criteria.filterGradeLevel)) {
            addCondition(whereClause, "sf.SF_GRADE_LEVEL = ?");
            params.add(criteria.filterGradeLevel);
        }
        if (!"All".equals(criteria.filterSection)) {
            addCondition(whereClause, "sf.SF_SECTION = ?");
            params.add(criteria.filterSection);
        }
        if (!"All".equals(criteria.filterTrackStrand)) {
            addCondition(whereClause, "sf.SF_TRACK_AND_STRAND = ?");
            params.add(criteria.filterTrackStrand);
        }
        if (!criteria.filterMale && criteria.filterFemale) {
            addCondition(whereClause, "s.STUDENT_SEX = ?"); params.add("Female");
        } else if (criteria.filterMale && !criteria.filterFemale) {
            addCondition(whereClause, "s.STUDENT_SEX = ?"); params.add("Male");
        } else if (!criteria.filterMale && !criteria.filterFemale) {
            addCondition(whereClause, "1 = 0");
        }
        addCondition(whereClause, "s.STUDENT_AGE BETWEEN ? AND ?");
        params.add(criteria.minAge);
        params.add(criteria.maxAge);


        if (whereClause.length() > 0) {
            sqlBuilder.append(" WHERE ").append(whereClause);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            // System.out.println("Executing Count SQL: " + pstmt.toString()); // For debugging
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error counting students: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }


    private void addCondition(StringBuilder whereClause, String condition) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(condition);
    }

    // Methods to fetch distinct values for filter dropdowns
    public Set<String> getDistinctGradeLevels() {
        Set<String> gradeLevels = new TreeSet<>(); // Use TreeSet for natural sorting
        String sql = "SELECT DISTINCT SF_GRADE_LEVEL FROM SCHOOL_FORM WHERE SF_GRADE_LEVEL IS NOT NULL AND SF_GRADE_LEVEL != '' ORDER BY SF_GRADE_LEVEL ASC";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) gradeLevels.add(rs.getString("SF_GRADE_LEVEL"));
        } catch (SQLException e) { e.printStackTrace(); JOptionPane.showMessageDialog(null, "Error fetching grade levels: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
        return gradeLevels;
    }
     public Set<String> getDistinctSections() {
        Set<String> sections = new TreeSet<>();
        String sql = "SELECT DISTINCT SF_SECTION FROM SCHOOL_FORM WHERE SF_SECTION IS NOT NULL AND SF_SECTION != '' ORDER BY SF_SECTION ASC";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) sections.add(rs.getString("SF_SECTION"));
        } catch (SQLException e) { e.printStackTrace(); JOptionPane.showMessageDialog(null, "Error fetching sections: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
        return sections;
    }


    public Set<String> getDistinctTrackStrands() {
        Set<String> trackStrands = new TreeSet<>();
        String sql = "SELECT DISTINCT SF_TRACK_AND_STRAND FROM SCHOOL_FORM WHERE SF_TRACK_AND_STRAND IS NOT NULL AND SF_TRACK_AND_STRAND != '' AND SF_TRACK_AND_STRAND != 'N/A' ORDER BY SF_TRACK_AND_STRAND ASC";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) trackStrands.add(rs.getString("SF_TRACK_AND_STRAND"));
        } catch (SQLException e) { e.printStackTrace(); JOptionPane.showMessageDialog(null, "Error fetching tracks/strands: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
        return trackStrands;
    }

    public int[] getMinMaxAge() {
        String sql = "SELECT MIN(STUDENT_AGE), MAX(STUDENT_AGE) FROM STUDENT";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int minAge = rs.getInt(1); int maxAge = rs.getInt(2);
                // Provide sensible defaults if DB values are 0
                return new int[]{minAge == 0 ? 12 : minAge, maxAge == 0 ? 22 : maxAge};
            }
        } catch (SQLException e) { e.printStackTrace(); JOptionPane.showMessageDialog(null, "Error fetching min/max age: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
        return new int[]{12, 22}; // Default fallback
    }
}

/**
 * FilterDialog Class
 * Provides a dialog for users to set advanced filter criteria.
 */
class FilterDialog extends JDialog {
    private final FilterCriteria currentFilterCriteria; // The criteria object to modify
    private final DatabaseManager dbManager;
    private JTextField firstNameField, lastNameField, middleNameField;
    private JCheckBox middleInitialOnlyCheckBox;
    private JComboBox<String> gradeLevelComboBox, sectionComboBox, trackStrandComboBox;
    private JCheckBox maleCheckBox, femaleCheckBox;
    private JSpinner minAgeSpinner, maxAgeSpinner;
    private int initialDbMinAge, initialDbMaxAge; // Store initial DB min/max ages for reset
    private boolean filtersApplied = false;

    public FilterDialog(Frame owner, FilterCriteria criteriaToModify, DatabaseManager dbManager) {
        super(owner, "Advanced Student Filters", true);
        this.currentFilterCriteria = criteriaToModify;
        this.dbManager = dbManager;

        // Fetch initial min/max ages from DB for spinner defaults and reset functionality
        int[] dbAges = dbManager.getMinMaxAge();
        initialDbMinAge = dbAges[0];
        initialDbMaxAge = dbAges[1];

        initComponents();
        loadCriteria(currentFilterCriteria); // Load existing criteria into dialog fields
        pack();
        setMinimumSize(new Dimension(500, getHeight())); // Adjusted width
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new MigLayout("insets dialog, fillx, wrap 2", "[right]para[grow,fill]", ""));

        // Name Filtering Section
        JPanel namePanel = new JPanel(new MigLayout("insets 0, fillx, wrap 2", "[right]para[grow,fill]", ""));
        namePanel.setBorder(new TitledBorder("Filter by Name"));
        namePanel.add(new JLabel("First Name:")); firstNameField = new JTextField(); namePanel.add(firstNameField, "growx");
        namePanel.add(new JLabel("Last Name:")); lastNameField = new JTextField(); namePanel.add(lastNameField, "growx");
        namePanel.add(new JLabel("Middle Name:")); middleNameField = new JTextField(); namePanel.add(middleNameField, "split 2, growx");
        middleInitialOnlyCheckBox = new JCheckBox("Initial Only"); namePanel.add(middleInitialOnlyCheckBox, "gapleft 5");
        add(namePanel, "span 2, growx, wrap unrel");

        // Academic Filtering Section
        JPanel academicPanel = new JPanel(new MigLayout("insets 0, fillx, wrap 2", "[right]para[grow,fill]", ""));
        academicPanel.setBorder(new TitledBorder("Filter by Academics"));
        DefaultComboBoxModel<String> gradeModel = new DefaultComboBoxModel<>(); gradeModel.addElement("All"); dbManager.getDistinctGradeLevels().forEach(gradeModel::addElement);
        gradeLevelComboBox = new JComboBox<>(gradeModel); academicPanel.add(new JLabel("Grade Level:")); academicPanel.add(gradeLevelComboBox, "growx");

        DefaultComboBoxModel<String> sectionModel = new DefaultComboBoxModel<>(); sectionModel.addElement("All"); dbManager.getDistinctSections().forEach(sectionModel::addElement);
        sectionComboBox = new JComboBox<>(sectionModel); academicPanel.add(new JLabel("Section:")); academicPanel.add(sectionComboBox, "growx");

        DefaultComboBoxModel<String> trackStrandModel = new DefaultComboBoxModel<>(); trackStrandModel.addElement("All"); dbManager.getDistinctTrackStrands().forEach(trackStrandModel::addElement);
        trackStrandComboBox = new JComboBox<>(trackStrandModel); academicPanel.add(new JLabel("Track & Strand:")); academicPanel.add(trackStrandComboBox, "growx");
        add(academicPanel, "span 2, growx, wrap unrel");

        // Demographics Filtering Section
        JPanel demoPanel = new JPanel(new MigLayout("insets 0, fillx, wrap 2", "[right]para[grow,fill]", ""));
        demoPanel.setBorder(new TitledBorder("Filter by Demographics"));
        maleCheckBox = new JCheckBox("Male"); femaleCheckBox = new JCheckBox("Female");
        JPanel sexPanel = new JPanel(new MigLayout("insets 0, gap 0", "[]10[]", "[]")); // Layout for sex checkboxes
        sexPanel.add(maleCheckBox); sexPanel.add(femaleCheckBox);
        demoPanel.add(new JLabel("Sex:")); demoPanel.add(sexPanel, "growx");

        // Age Spinners
        // Use initialDbMinAge and initialDbMaxAge for spinner bounds
        SpinnerNumberModel minModel = new SpinnerNumberModel(initialDbMinAge, initialDbMinAge, Math.max(initialDbMinAge, initialDbMaxAge + 20), 1);
        minAgeSpinner = new JSpinner(minModel);
        SpinnerNumberModel maxModel = new SpinnerNumberModel(Math.min(initialDbMaxAge + 5, initialDbMaxAge + 20), initialDbMinAge, Math.max(initialDbMinAge, initialDbMaxAge + 20), 1);
        maxAgeSpinner = new JSpinner(maxModel);

        JPanel agePanel = new JPanel(new MigLayout("insets 0, fillx", "[][grow,fill][][]", ""));
        agePanel.add(new JLabel("Min:")); agePanel.add(minAgeSpinner, "w 60!");
        agePanel.add(new JLabel("Max:"), "gapleft 15"); agePanel.add(maxAgeSpinner, "w 60!");
        demoPanel.add(new JLabel("Age Range:")); demoPanel.add(agePanel, "growx");
        add(demoPanel, "span 2, growx, wrap unrel");

        // Action Buttons
        JButton applyButton = new JButton("Apply Filters"); applyButton.addActionListener(e -> applyFilters());
        JButton clearButton = new JButton("Clear Filters"); clearButton.addActionListener(e -> clearFiltersAndApply());
        JButton cancelButton = new JButton("Cancel"); cancelButton.addActionListener(e -> { filtersApplied = false; setVisible(false); dispose(); });

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, align right")); // Align buttons to the right
        buttonPanel.add(applyButton);
        buttonPanel.add(clearButton, "gapleft 5");
        buttonPanel.add(cancelButton, "gapleft 5");
        add(buttonPanel, "span 2, align right, gaptop 15");
    }

    /**
     * Loads the current filter criteria into the dialog's input fields.
     * @param criteria The FilterCriteria object to load from.
     */
    private void loadCriteria(FilterCriteria criteria) {
        firstNameField.setText(criteria.filterFirstName);
        lastNameField.setText(criteria.filterLastName);
        middleNameField.setText(criteria.filterMiddleName);
        middleInitialOnlyCheckBox.setSelected(criteria.middleInitialOnly);

        gradeLevelComboBox.setSelectedItem(criteria.filterGradeLevel);
        sectionComboBox.setSelectedItem(criteria.filterSection);

        // Handle trackStrandComboBox item existence
        if (criteria.filterTrackStrand == null || ((DefaultComboBoxModel<String>)trackStrandComboBox.getModel()).getIndexOf(criteria.filterTrackStrand) == -1) {
             trackStrandComboBox.setSelectedItem("All");
        } else {
            trackStrandComboBox.setSelectedItem(criteria.filterTrackStrand);
        }

        maleCheckBox.setSelected(criteria.filterMale);
        femaleCheckBox.setSelected(criteria.filterFemale);

        // Safely set spinner values within their model's bounds
        SpinnerNumberModel minModel = (SpinnerNumberModel) minAgeSpinner.getModel();
        minAgeSpinner.setValue(Math.max((Integer)minModel.getMinimum(), Math.min(criteria.minAge, (Integer)minModel.getMaximum())));

        SpinnerNumberModel maxModel = (SpinnerNumberModel) maxAgeSpinner.getModel();
        maxAgeSpinner.setValue(Math.min((Integer)maxModel.getMaximum(), Math.max(criteria.maxAge, (Integer)maxModel.getMinimum())));
    }

    /**
     * Applies the filters set in the dialog to the currentFilterCriteria object.
     */
    private void applyFilters() {
        currentFilterCriteria.filterFirstName = firstNameField.getText().trim();
        currentFilterCriteria.filterLastName = lastNameField.getText().trim();
        currentFilterCriteria.filterMiddleName = middleNameField.getText().trim();
        currentFilterCriteria.middleInitialOnly = middleInitialOnlyCheckBox.isSelected();
        currentFilterCriteria.filterGradeLevel = (String) gradeLevelComboBox.getSelectedItem();
        currentFilterCriteria.filterSection = (String) sectionComboBox.getSelectedItem();
        currentFilterCriteria.filterTrackStrand = (String) trackStrandComboBox.getSelectedItem();
        currentFilterCriteria.filterMale = maleCheckBox.isSelected();
        currentFilterCriteria.filterFemale = femaleCheckBox.isSelected();
        currentFilterCriteria.minAge = (Integer) minAgeSpinner.getValue();
        currentFilterCriteria.maxAge = (Integer) maxAgeSpinner.getValue();

        filtersApplied = true;
        setVisible(false);
        dispose();
    }

    /**
     * Clears all filter fields in the dialog and applies these cleared filters.
     */
    private void clearFiltersAndApply() {
        // Reset UI fields
        firstNameField.setText(""); lastNameField.setText(""); middleNameField.setText("");
        middleInitialOnlyCheckBox.setSelected(false);
        gradeLevelComboBox.setSelectedItem("All");
        sectionComboBox.setSelectedItem("All");
        trackStrandComboBox.setSelectedItem("All");
        maleCheckBox.setSelected(true); femaleCheckBox.setSelected(true);
        minAgeSpinner.setValue(initialDbMinAge); // Reset to initial DB min age
        maxAgeSpinner.setValue(initialDbMaxAge > initialDbMinAge ? initialDbMaxAge + 5 : initialDbMinAge + 20); // Reset to default max age logic

        // Reset the underlying FilterCriteria object
        currentFilterCriteria.reset(initialDbMinAge, initialDbMaxAge);

        filtersApplied = true;
        setVisible(false);
        dispose();
    }

    /**
     * Checks if the filters were applied by the user (i.e., "Apply" or "Clear" was clicked).
     * @return True if filters were applied, false otherwise (e.g., dialog was cancelled).
     */
    public boolean wereFiltersApplied() { return filtersApplied; }
}


/**
 * StudentApp Class
 * Main application window for viewing and filtering student data.
 */
public class StudentApp extends JFrame {
    private final DatabaseManager dbManager;
    private GTable studentTable;
    private final FilterCriteria currentFilters;
    private List<Student> currentStudentList; // Holds the currently displayed list of students

    private int currentPageSize = 25; // Default page size
    private final Integer[] availablePageSizes = {10, 25, 50, 100, 250};

    private JTextField searchField;
    private JLabel appliedFiltersLabel;
    private JComboBox<Integer> pageSizeComboBox;
    private JButton printSelectedButton; // New button


    private int dbMinAge, dbMaxAge; // Store min/max age from DB for filter label

    public StudentApp() {
        dbManager = new DatabaseManager();
        currentFilters = new FilterCriteria();
        currentStudentList = new ArrayList<>();

        // Initialize min/max ages from DB
        int[] dbAges = dbManager.getMinMaxAge();
        dbMinAge = dbAges[0];
        dbMaxAge = dbAges[1];
        currentFilters.minAge = dbMinAge;
        currentFilters.maxAge = dbMaxAge > dbMinAge ? dbMaxAge + 5 : dbMinAge + 20;


        setTitle("Student List Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        loadData();
        pack();
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new MigLayout("insets dialog, fill", "[grow,fill]", "[][grow,fill][]"));
        mainPanel.setBorder(new EmptyBorder(10,10,10,10)); // Increased padding

        // --- Top Panel for Search, Filters, and Page Size ---
        JPanel topPanel = new JPanel(new MigLayout("insets 0, fillx", "[][grow,fill]para[]para[]para[right]para[]para[]", "[]")); // Added one more para[] for the new button
        topPanel.add(new JLabel("Search LRN/Name:"));
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Enter LRN or any part of name...");
        topPanel.add(searchField, "growx");

        JButton searchButton = new JButton("Search");
        searchButton.setToolTipText("Apply search term");
        searchButton.addActionListener(e -> {
            currentFilters.searchTerm = searchField.getText().trim();
            // Reset detailed name filters if a general search term is used
            currentFilters.filterFirstName = "";
            currentFilters.filterLastName = "";
            currentFilters.filterMiddleName = "";
            loadData();
        });
        topPanel.add(searchButton);

        JButton filterButton = new JButton("Advanced Filters...");
        filterButton.setToolTipText("Open advanced filter options");
        filterButton.addActionListener(e -> openFilterDialog());
        topPanel.add(filterButton);
        
        // New "Print Selected" button
        printSelectedButton = new JButton("Print Selected");
        printSelectedButton.setToolTipText("Print LRN and Name of selected students");
        printSelectedButton.addActionListener(e -> printSelectedStudents());
        topPanel.add(printSelectedButton, "gapleft 10"); // Add some gap

        appliedFiltersLabel = new JLabel("Filters: 0 | Total: 0");
        topPanel.add(appliedFiltersLabel, "gapleft push");

        pageSizeComboBox = new JComboBox<>(availablePageSizes);
        pageSizeComboBox.setSelectedItem(currentPageSize);
        pageSizeComboBox.setToolTipText("Select records per page");
        pageSizeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                JComboBox<?> sourceComboBox = (JComboBox<?>) e.getSource();
                currentPageSize = (Integer) sourceComboBox.getSelectedItem();
                studentTable.setPaginationEnabled(true, currentPageSize);
                loadData(); 
            }
        });
        topPanel.add(new JLabel("Rows:"), "gapleft 10");
        topPanel.add(pageSizeComboBox);

        mainPanel.add(topPanel, "wrap");

        // --- GTable Setup ---
        // Added "Select" column for checkbox
        String[] columnNames = {"Select", "LRN", "Name", "Sex", "Age", "Grade", "Section", "Track/Strand", "Actions"};
        Class<?>[] columnTypes = {Boolean.class, String.class, String.class, String.class, Integer.class, String.class, String.class, String.class, Object.class};
        boolean[] editableColumns = {true, false, false, false, false, false, false, false, true}; // Checkbox is editable

        // Adjusted column width proportions for the new "Select" column
        double[] columnWidthProportions = {0.05, 0.12, 0.20, 0.05, 0.05, 0.08, 0.10, 0.15, 0.15}; // Sum should be close to 1.0 if actions column is flexible

        int[] alignments = {
            SwingConstants.CENTER, // Select (Checkbox)
            SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER,
            SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER
        };

        // --- Action Manager Setup ---
        DefaultTableActionManager actionManager = new DefaultTableActionManager();
        actionManager.addAction("View", (table, row) -> {
            // LRN is now at index 1 because of the checkbox column at index 0
            String lrn = (String) studentTable.getModel().getValueAt(row, 1); 
            Student student = findStudentByLrn(lrn); // findStudentByLrn needs to be aware of currentStudentList content
            if (student != null) {
                displayStudentDetails(student);
            } else {
                 JOptionPane.showMessageDialog(StudentApp.this,"Could not find student details for LRN: " + lrn, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }, new Color(0, 123, 255), null); 

        actionManager.addAction("Edit", (table, row) -> {
            String lrn = (String) studentTable.getModel().getValueAt(row, 1); // LRN is at index 1
            JOptionPane.showMessageDialog(StudentApp.this, "Edit action for LRN: " + lrn + "\n(Not implemented yet)", "Edit Student", JOptionPane.INFORMATION_MESSAGE);
        }, new Color(255, 193, 7), null);


        studentTable = new GTable(new Object[][]{}, columnNames, columnTypes, editableColumns, columnWidthProportions, alignments, true, actionManager); // Set includeCheckbox to true
        studentTable.setPaginationEnabled(true, currentPageSize);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        mainPanel.add(scrollPane, "grow, wrap");
        mainPanel.add(studentTable.getPaginationPanel(), "dock south, align center");

        setContentPane(mainPanel);
    }
    
    /**
     * Action handler for the "Print Selected" button.
     * Iterates through the table, finds selected students, and prints their LRN and Name.
     */
    private void printSelectedStudents() {
        StringBuilder selectedStudentsInfo = new StringBuilder("Selected Students:\n");
        boolean anySelected = false;
        // GTable paginates data, so getRowCount() gives rows on the current page.
        // If you need to get all selected items across all pages, GTable would need a method for that,
        // or you'd manage selections against the full currentStudentList.
        // For this implementation, we process the current view of the table.
        for (int i = 0; i < studentTable.getRowCount(); i++) {
            Boolean isSelected = (Boolean) studentTable.getValueAt(i, 0); // Checkbox is at column 0
            if (isSelected != null && isSelected) {
                anySelected = true;
                String lrn = (String) studentTable.getValueAt(i, 1);    // LRN is at column 1
                String name = (String) studentTable.getValueAt(i, 2);   // Name is at column 2
                selectedStudentsInfo.append("LRN: ").append(lrn).append(", Name: ").append(name).append("\n");
            }
        }

        if (anySelected) {
            System.out.println(selectedStudentsInfo.toString()); // Print to console
            // You can also display this in a JOptionPane or a JTextArea in a dialog
            JTextArea textArea = new JTextArea(selectedStudentsInfo.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            JOptionPane.showMessageDialog(this, scrollPane, "Selected Student Details", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No students selected.", "Print Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /**
     * Finds a student in the currentStudentList by their LRN.
     * This list (currentStudentList) holds all students matching the current filter,
     * not just the ones on the current page of GTable.
     * @param lrn The LRN to search for.
     * @return The Student object if found, null otherwise.
     */
    private Student findStudentByLrn(String lrn) {
        if (lrn == null) return null;
        for (Student student : currentStudentList) { // Search the master list
            if (lrn.equals(student.getLrn())) {
                return student;
            }
        }
        return null;
    }

    /**
     * Loads data from the database based on current filters and updates the GTable.
     */
    private void loadData() {
        // Fetch ALL students matching the current filters. GTable will handle pagination.
        currentStudentList = dbManager.getStudents(1, Integer.MAX_VALUE, currentFilters);

        // The number of columns in dataForGTable must match studentTable.getColumnCount()
        // which is now 9 (Select, LRN, Name, Sex, Age, Grade, Section, Track/Strand, Actions)
        Object[][] dataForGTable = new Object[currentStudentList.size()][studentTable.getColumnCount()];
        for (int i = 0; i < currentStudentList.size(); i++) {
            Student s = currentStudentList.get(i);
            dataForGTable[i] = new Object[]{
                Boolean.FALSE, // For the "Select" checkbox column
                s.getLrn(), 
                s.getFullName(), 
                s.getSex(), 
                s.getAge(),
                s.getGradeLevel(), 
                s.getSection(), 
                s.getTrackAndStrand(),
                "" // Placeholder for actions column, GTable's ActionManager handles rendering
            };
        }
        studentTable.setData(dataForGTable); // This will internally handle pagination display
        updateAppliedFiltersLabel();
    }

    /**
     * Updates the label displaying the count of active filters and total records.
     */
    private void updateAppliedFiltersLabel() {
        int filterCount = currentFilters.getActiveFilterCount(dbMinAge, dbMaxAge);
        int totalRecords = currentStudentList.size(); // Total records matching filter, before GTable pagination

        String filterText = filterCount == 1 ? "1 Active Filter" : filterCount + " Active Filters";
        String recordText = totalRecords == 1 ? "1 Record Found" : totalRecords + " Records Found";

        if (filterCount == 0 && (currentFilters.searchTerm == null || currentFilters.searchTerm.isEmpty())) {
            appliedFiltersLabel.setText("Showing All Records: " + recordText);
        } else if (filterCount == 0 && currentFilters.searchTerm != null && !currentFilters.searchTerm.isEmpty()){
             appliedFiltersLabel.setText("Search Active | " + recordText);
        }
        else {
            appliedFiltersLabel.setText(filterText + " | " + recordText);
        }
    }

    /**
     * Opens the advanced filter dialog.
     */
    private void openFilterDialog() {
        searchField.setText(""); 
        currentFilters.searchTerm = ""; 

        FilterDialog dialog = new FilterDialog(this, currentFilters, dbManager);
        dialog.setVisible(true);

        if (dialog.wereFiltersApplied()) {
            loadData();
        }
    }

    /**
     * Displays student details in a more structured dialog.
     * @param student The student whose details are to be displayed.
     */
    private void displayStudentDetails(Student student) {
        JPanel detailsPanel = new JPanel(new MigLayout("wrap 2, insets 15", "[right]para[grow,fill]"));
        detailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        detailsPanel.add(new JLabel("LRN:")); detailsPanel.add(new JLabel(student.getLrn()));
        detailsPanel.add(new JLabel("Full Name:")); detailsPanel.add(new JLabel(student.getFullName()));
        detailsPanel.add(new JLabel("Formatted Name:")); detailsPanel.add(new JLabel(student.getFormattedName())); 
        detailsPanel.add(new JLabel("Sex:")); detailsPanel.add(new JLabel(student.getSex()));
        detailsPanel.add(new JLabel("Age:")); detailsPanel.add(new JLabel(String.valueOf(student.getAge())));
        detailsPanel.add(new JLabel("Grade Level:")); detailsPanel.add(new JLabel(student.getGradeLevel()));
        detailsPanel.add(new JLabel("Section:")); detailsPanel.add(new JLabel(student.getSection()));
        detailsPanel.add(new JLabel("Track/Strand:"));

        JTextArea trackStrandArea = new JTextArea(student.getTrackAndStrand());
        trackStrandArea.setEditable(false);
        trackStrandArea.setLineWrap(true);
        trackStrandArea.setWrapStyleWord(true);
        trackStrandArea.setBackground(detailsPanel.getBackground()); 
        JScrollPane trackScrollPane = new JScrollPane(trackStrandArea);
        trackScrollPane.setBorder(null); 
        trackScrollPane.setPreferredSize(new Dimension(250, 40)); 
        detailsPanel.add(trackScrollPane, "growx");


        JOptionPane.showMessageDialog(this, detailsPanel, "Student Details: " + student.getFirstName(), JOptionPane.INFORMATION_MESSAGE);
    }


    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) { e.printStackTrace(); }
        }

        SwingUtilities.invokeLater(() -> {
            StudentApp app = new StudentApp();
            app.setVisible(true);
        });
    }
}

class DBConnection {
	//THIS IS LITERALLY MY DATABASE SETUP NO NEED TO CHANGE IT 
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/gomisdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YourRootPassword123!"; 

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "MariaDB JDBC Driver not found. Please add it to your project's classpath.",
                "Driver Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Database Connection Error: " + ex.getMessage() +
                "\nPlease check your database server (" + DB_URL + ") and credentials.",
                "DB Connection Error", JOptionPane.ERROR_MESSAGE);
        }
        return connection;
    }
}
