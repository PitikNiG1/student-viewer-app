package tableList;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

// FlatLaf Imports
import com.formdev.flatlaf.FlatLightLaf; // Or FlatDarkLaf, FlatIntelliJLaf, etc.

// MigLayout Imports
import net.miginfocom.swing.MigLayout;

/**
 * Student POJO Class
 * Represents a student with relevant information.
 */
class Student {
    private String lrn;
    private String lastName;
    private String firstName;
    private String middleName;
    private String sex;
    // private LocalDate birthDate; // Not directly used if age is provided
    private int age; // Directly from STUDENT_AGE if available
    private String gradeLevel;
    private String section;
    private String trackAndStrand;

    // Constructor
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

    public String getFullName() {
        return lastName + ", " + firstName + " " + (middleName != null && !middleName.isEmpty() ? middleName.charAt(0) + "." : "");
    }

    @Override
    public String toString() {
        return "Student{" +
               "lrn='" + lrn + '\'' +
               ", lastName='" + lastName + '\'' +
               ", firstName='" + firstName + '\'' +
               ", age=" + age +
               ", gradeLevel='" + gradeLevel + '\'' +
               '}';
    }
}

/**
 * Filter Class
 * Encapsulates all filter criteria.
 */
class FilterCriteria {
    String searchNameTerm = "";
    String filterFirstName = "";
    String filterLastName = "";
    String filterMiddleName = "";
    boolean middleInitialOnly = false;
    String filterGradeLevel = "All";
    String filterTrackStrand = "All";
    boolean filterMale = true;
    boolean filterFemale = true;
    int minAge = 0;
    int maxAge = 100;

    public FilterCriteria() {}

    // getAppliedFilterCount is now more accurately handled in StudentApp.updateAppliedFiltersLabel()
    // as it needs access to dbManager.getMinMaxAge() for accurate age filter counting.

    public void reset(int dbMinAge, int dbMaxAge) { // Pass DB min/max for accurate age reset
        searchNameTerm = "";
        filterFirstName = "";
        filterLastName = "";
        filterMiddleName = "";
        middleInitialOnly = false;
        filterGradeLevel = "All";
        filterTrackStrand = "All";
        filterMale = true;
        filterFemale = true;
        minAge = dbMinAge;
        maxAge = dbMaxAge + 5;
    }
}

/**
 * StudentTableModel Class
 * Custom TableModel for displaying student data.
 */
class StudentTableModel extends AbstractTableModel {
    private final List<Student> students;
    private final String[] columnNames = {"LRN", "Last Name", "First Name", "Middle Name", "Sex", "Age", "Grade Level", "Section", "Track & Strand"};

    public StudentTableModel(List<Student> students) {
        this.students = students != null ? new ArrayList<>(students) : new ArrayList<>();
    }

    public void setData(List<Student> newStudents) {
        this.students.clear();
        if (newStudents != null) {
            this.students.addAll(newStudents);
        }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student student = students.get(rowIndex);
        switch (columnIndex) {
            case 0: return student.getLrn();
            case 1: return student.getLastName();
            case 2: return student.getFirstName();
            case 3: return student.getMiddleName();
            case 4: return student.getSex();
            case 5: return student.getAge();
            case 6: return student.getGradeLevel();
            case 7: return student.getSection();
            case 8: return student.getTrackAndStrand();
            default: return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
}

/**
 * Simulated DatabaseManager Class
 * Manages student data, including filtering and pagination.
 */
class DatabaseManager {
    private final List<Student> allStudents = new ArrayList<>();
    private static final Random random = new Random();

    public DatabaseManager() {
        generateDummyStudents(4000);
    }

    private void generateDummyStudents(int count) {
        String[] firstNamesMale = {"James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Charles", "Thomas"};
        String[] firstNamesFemale = {"Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Susan", "Jessica", "Sarah", "Karen", "Nancy"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Lee", "Walker", "Hall", "Allen", "King"};
        String[] middleInitials = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", ""};
        String[] sexes = {"Male", "Female"};
        String[] gradeLevels = {"Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12"};
        String[] sections = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};
        String[] tracks = {"Academic", "TVL", "Sports", "Arts and Design"};
        String[] strandsAcademic = {"ABM", "STEM", "HUMSS", "GAS"};
        String[] strandsTVL = {"ICT", "HE", "IA", "Agri-Fishery"};

        for (int i = 0; i < count; i++) {
            String sex = sexes[random.nextInt(sexes.length)];
            String firstName = sex.equals("Male") ? firstNamesMale[random.nextInt(firstNamesMale.length)] : firstNamesFemale[random.nextInt(firstNamesFemale.length)];
            String lastNameVal = lastNames[random.nextInt(lastNames.length)];
            String currentMiddleNameInitial = middleInitials[random.nextInt(middleInitials.length)]; // Renamed for clarity
            String fullMiddleName = currentMiddleNameInitial; // Start with the initial

            if (!currentMiddleNameInitial.isEmpty() && random.nextBoolean()) { // 50% chance of having a full middle name if initial is present
                String lastNameForMiddlePart = lastNames[random.nextInt(lastNames.length)];
                int lengthForMiddlePart = lastNameForMiddlePart.length();
                // Ensure substring length is valid for lastNameForMiddlePart
                int charsToTake = Math.min(3 + random.nextInt(4), lengthForMiddlePart);
                if (lengthForMiddlePart > 0 && charsToTake > 0) { // Add part only if there's something to take
                     fullMiddleName = currentMiddleNameInitial + lastNameForMiddlePart.substring(0, charsToTake).toLowerCase();
                }
            }

            String lrn = String.format("%012d", Math.abs(random.nextLong() % 1000000000000L));
            int age = 12 + random.nextInt(10); // Ages 12-21
            String gradeLevel = gradeLevels[random.nextInt(gradeLevels.length)];
            String section = sections[random.nextInt(sections.length)];
            String track = "";
            String strand = "";
            if (gradeLevel.contains(" ")) { // Basic check to avoid error if gradeLevel format changes
                try {
                    if (Integer.parseInt(gradeLevel.split(" ")[1]) >= 11) { // SHS
                        track = tracks[random.nextInt(tracks.length)];
                        if (track.equals("Academic")) {
                            strand = strandsAcademic[random.nextInt(strandsAcademic.length)];
                        } else if (track.equals("TVL")) {
                            strand = strandsTVL[random.nextInt(strandsTVL.length)];
                        }
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Handle potential errors if gradeLevel string is not as expected
                    // For example, log this or assign a default track/strand
                    System.err.println("Warning: Could not parse grade level for track/strand: " + gradeLevel);
                }
            }
            String trackAndStrand = track.isEmpty() ? "N/A" : (track + (strand.isEmpty() ? "" : " - " + strand));

            allStudents.add(new Student(lrn, lastNameVal, firstName, fullMiddleName, sex, age, gradeLevel, section, trackAndStrand));
        }
    }

    public List<Student> getStudents(int page, int pageSize, FilterCriteria criteria) {
        List<Student> filteredStudents = allStudents.stream()
            .filter(s -> {
                boolean match = true;
                if (criteria.searchNameTerm != null && !criteria.searchNameTerm.isEmpty()) {
                    String searchTermLower = criteria.searchNameTerm.toLowerCase();
                    match = s.getFirstName().toLowerCase().contains(searchTermLower) ||
                            s.getLastName().toLowerCase().contains(searchTermLower) ||
                            s.getMiddleName().toLowerCase().contains(searchTermLower);
                }
                if (!match) return false;

                if (criteria.filterFirstName != null && !criteria.filterFirstName.isEmpty()) {
                    match = s.getFirstName().toLowerCase().contains(criteria.filterFirstName.toLowerCase());
                }
                if (!match) return false;

                if (criteria.filterLastName != null && !criteria.filterLastName.isEmpty()) {
                    match = s.getLastName().toLowerCase().contains(criteria.filterLastName.toLowerCase());
                }
                if (!match) return false;

                if (criteria.filterMiddleName != null && !criteria.filterMiddleName.isEmpty()) {
                    String middleNameLower = s.getMiddleName().toLowerCase();
                    String filterMiddleLower = criteria.filterMiddleName.toLowerCase();
                    if (criteria.middleInitialOnly) {
                        if (middleNameLower.isEmpty() || filterMiddleLower.isEmpty()) match = middleNameLower.isEmpty() && filterMiddleLower.isEmpty();
                        else match = middleNameLower.startsWith(String.valueOf(filterMiddleLower.charAt(0)));
                    } else {
                        match = middleNameLower.contains(filterMiddleLower);
                    }
                }
                if (!match) return false;

                if (!"All".equals(criteria.filterGradeLevel)) {
                    match = s.getGradeLevel().equals(criteria.filterGradeLevel);
                }
                if (!match) return false;

                if (!"All".equals(criteria.filterTrackStrand)) {
                    match = s.getTrackAndStrand().equals(criteria.filterTrackStrand);
                }
                if (!match) return false;

                boolean maleSelected = criteria.filterMale;
                boolean femaleSelected = criteria.filterFemale;
                if (maleSelected && !femaleSelected) {
                    match = "Male".equalsIgnoreCase(s.getSex());
                } else if (!maleSelected && femaleSelected) {
                    match = "Female".equalsIgnoreCase(s.getSex());
                } else if (!maleSelected && !femaleSelected) {
                    match = false;
                }
                if (!match) return false;

                match = s.getAge() >= criteria.minAge && s.getAge() <= criteria.maxAge;
                return match;
            })
            .sorted(Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName))
            .collect(Collectors.toList());

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filteredStudents.size());

        if (fromIndex >= filteredStudents.size() || fromIndex < 0) {
            return new ArrayList<>();
        }
        return filteredStudents.subList(fromIndex, toIndex);
    }

    public int getTotalStudentCount(FilterCriteria criteria) {
        return (int) allStudents.stream()
            .filter(s -> {
                boolean match = true;
                if (criteria.searchNameTerm != null && !criteria.searchNameTerm.isEmpty()) {
                    String searchTermLower = criteria.searchNameTerm.toLowerCase();
                    match = s.getFirstName().toLowerCase().contains(searchTermLower) ||
                            s.getLastName().toLowerCase().contains(searchTermLower) ||
                            s.getMiddleName().toLowerCase().contains(searchTermLower);
                }
                if (!match) return false;

                if (criteria.filterFirstName != null && !criteria.filterFirstName.isEmpty()) {
                    match = s.getFirstName().toLowerCase().contains(criteria.filterFirstName.toLowerCase());
                }
                if (!match) return false;

                if (criteria.filterLastName != null && !criteria.filterLastName.isEmpty()) {
                    match = s.getLastName().toLowerCase().contains(criteria.filterLastName.toLowerCase());
                }
                if (!match) return false;

                if (criteria.filterMiddleName != null && !criteria.filterMiddleName.isEmpty()) {
                    String middleNameLower = s.getMiddleName().toLowerCase();
                    String filterMiddleLower = criteria.filterMiddleName.toLowerCase();
                    if (criteria.middleInitialOnly) {
                        if (middleNameLower.isEmpty() || filterMiddleLower.isEmpty()) match = middleNameLower.isEmpty() && filterMiddleLower.isEmpty();
                        else match = middleNameLower.startsWith(String.valueOf(filterMiddleLower.charAt(0)));
                    } else {
                        match = middleNameLower.contains(filterMiddleLower);
                    }
                }
                 if (!match) return false;

                if (!"All".equals(criteria.filterGradeLevel)) {
                    match = s.getGradeLevel().equals(criteria.filterGradeLevel);
                }
                 if (!match) return false;

                if (!"All".equals(criteria.filterTrackStrand)) {
                    match = s.getTrackAndStrand().equals(criteria.filterTrackStrand);
                }
                 if (!match) return false;

                boolean maleSelected = criteria.filterMale;
                boolean femaleSelected = criteria.filterFemale;
                if (maleSelected && !femaleSelected) {
                    match = "Male".equalsIgnoreCase(s.getSex());
                } else if (!maleSelected && femaleSelected) {
                    match = "Female".equalsIgnoreCase(s.getSex());
                } else if (!maleSelected && !femaleSelected) {
                    match = false;
                }
                 if (!match) return false;

                match = s.getAge() >= criteria.minAge && s.getAge() <= criteria.maxAge;
                return match;
            })
            .count();
    }

    public Set<String> getDistinctGradeLevels() {
        return allStudents.stream()
                          .map(Student::getGradeLevel)
                          .filter(Objects::nonNull)
                          .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<String> getDistinctTrackStrands() {
        return allStudents.stream()
                          .map(Student::getTrackAndStrand)
                          .filter(s -> Objects.nonNull(s) && !s.isEmpty() && !"N/A".equals(s)) // Exclude N/A from distinct options
                          .collect(Collectors.toCollection(TreeSet::new));
    }

    public int[] getMinMaxAge() {
        if (allStudents.isEmpty()) {
            return new int[]{12, 22}; // Default if no students, reasonable for high school
        }
        int minAge = allStudents.stream().mapToInt(Student::getAge).min().orElse(12);
        int maxAge = allStudents.stream().mapToInt(Student::getAge).max().orElse(22);
        return new int[]{minAge, maxAge};
    }
}

/**
 * FilterDialog Class
 * Dialog for setting advanced filter criteria using MigLayout.
 */
class FilterDialog extends JDialog {
    private final FilterCriteria currentFilterCriteria; // This is the reference from StudentApp
    private final DatabaseManager dbManager;

    private JTextField firstNameField, lastNameField, middleNameField;
    private JCheckBox middleInitialOnlyCheckBox;
    private JComboBox<String> gradeLevelComboBox;
    private JComboBox<String> trackStrandComboBox;
    private JCheckBox maleCheckBox, femaleCheckBox;
    private JSpinner minAgeSpinner, maxAgeSpinner;

    private final int initialDbMinAge;
    private final int initialDbMaxAge;

    private boolean filtersApplied = false;

    public FilterDialog(Frame owner, FilterCriteria criteriaToModify, DatabaseManager dbManager) {
        super(owner, "Filter Students", true);
        this.currentFilterCriteria = criteriaToModify; // Directly modify the shared criteria object
        this.dbManager = dbManager;

        int[] ages = dbManager.getMinMaxAge();
        initialDbMinAge = ages[0];
        initialDbMaxAge = ages[1];

        initComponents();
        pack();
        setLocationRelativeTo(owner);
        // After packing, set a minimum size if needed
        setMinimumSize(new Dimension(450, getHeight()));
    }

    private void initComponents() {
        // Layout: "insets 10, fillx" - 10px padding around, components fill horizontally by default
        // Columns: "[right]para[grow,fill]" - First column for labels (right-aligned), paragraph gap, second column for fields (grows and fills)
        // Rows: multiple rows with default gaps, last row for buttons
        setLayout(new MigLayout("insets 10, fillx", "[right]para[grow,fill]", ""));

        // Name Filters
        add(new JLabel("Filter by Name:"), "span 2, wrap unrel"); // span 2 columns, wrap to next line with unrelated gap

        add(new JLabel("First Name:"), "gapbottom 5"); // small gap below label
        firstNameField = new JTextField();
        add(firstNameField, "wrap"); // wrap to next line

        add(new JLabel("Last Name:"), "gapbottom 5");
        lastNameField = new JTextField();
        add(lastNameField, "wrap");

        add(new JLabel("Middle Name:"), "gapbottom 5");
        middleNameField = new JTextField();
        add(middleNameField, "split 2"); // split the cell for this and checkbox
        middleInitialOnlyCheckBox = new JCheckBox("Initial Only");
        add(middleInitialOnlyCheckBox, "gapleft 5, wrap"); // small gap to its left, then wrap

        // Grade Level Filter
        add(new JLabel("Grade Level:"), "gapbottom 5");
        DefaultComboBoxModel<String> gradeModel = new DefaultComboBoxModel<>();
        gradeModel.addElement("All");
        dbManager.getDistinctGradeLevels().forEach(gradeModel::addElement);
        gradeLevelComboBox = new JComboBox<>(gradeModel);
        add(gradeLevelComboBox, "wrap");

        // Track & Strand Filter
        add(new JLabel("Track & Strand:"), "gapbottom 5");
        DefaultComboBoxModel<String> trackStrandModel = new DefaultComboBoxModel<>();
        trackStrandModel.addElement("All");
        dbManager.getDistinctTrackStrands().forEach(trackStrandModel::addElement);
        trackStrandComboBox = new JComboBox<>(trackStrandModel);
        add(trackStrandComboBox, "wrap");

        // Sex Filter
        add(new JLabel("Sex:"), "gapbottom 5");
        maleCheckBox = new JCheckBox("Male");
        femaleCheckBox = new JCheckBox("Female");
        // Using a panel for checkboxes to keep them together, then adding the panel
        JPanel sexPanel = new JPanel(new MigLayout("insets 0, gap 0")); // No insets for this inner panel
        sexPanel.add(maleCheckBox);
        sexPanel.add(femaleCheckBox, "gapleft 10"); // Gap between checkboxes
        add(sexPanel, "wrap");


        // Age Range Filter
        add(new JLabel("Age Range:"), "gapbottom 5");
        minAgeSpinner = new JSpinner(new SpinnerNumberModel(initialDbMinAge, initialDbMinAge, initialDbMaxAge + 20, 1)); // Increased upper bound for spinner model
        maxAgeSpinner = new JSpinner(new SpinnerNumberModel(initialDbMaxAge + 5, initialDbMinAge, initialDbMaxAge + 20, 1));
        // Panel for age spinners
        JPanel agePanel = new JPanel(new MigLayout("insets 0, fillx", "[][grow,fill][][]", "")); // Layout for Min: [spinner] Max: [spinner]
        agePanel.add(new JLabel("Min:"));
        agePanel.add(minAgeSpinner, "w 60!"); // Set preferred width for spinner
        agePanel.add(new JLabel("Max:"), "gapleft 15");
        agePanel.add(maxAgeSpinner, "w 60!, wrap"); // Set preferred width and wrap
        add(agePanel, "span 2, growx, wrap unrel"); // Span both columns, grow horizontally, unrelated gap


        // Load initial criteria
        loadCriteria(currentFilterCriteria);

        // Buttons Panel
        JButton applyButton = new JButton("Apply Filters");
        applyButton.addActionListener(e -> applyFilters());
        JButton clearButton = new JButton("Clear Filters");
        clearButton.addActionListener(e -> clearFiltersAndApply());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> { filtersApplied = false; setVisible(false); dispose(); });

        // Add buttons to a panel with MigLayout for right alignment
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, align right"));
        buttonPanel.add(applyButton);
        buttonPanel.add(clearButton, "gapleft 5");
        buttonPanel.add(cancelButton, "gapleft 5");

        add(buttonPanel, "span 2, align right, gaptop 15"); // span 2 columns, align right, gap above
    }

    private void loadCriteria(FilterCriteria criteria) {
        firstNameField.setText(criteria.filterFirstName);
        lastNameField.setText(criteria.filterLastName);
        middleNameField.setText(criteria.filterMiddleName);
        middleInitialOnlyCheckBox.setSelected(criteria.middleInitialOnly);
        gradeLevelComboBox.setSelectedItem(criteria.filterGradeLevel);

        // Corrected line for trackStrandComboBox:
        if (criteria.filterTrackStrand == null || 
            ((DefaultComboBoxModel<String>)trackStrandComboBox.getModel()).getIndexOf(criteria.filterTrackStrand) != -1) {
             trackStrandComboBox.setSelectedItem(criteria.filterTrackStrand);
        } else {
            trackStrandComboBox.setSelectedItem("All"); // Default if previous selection not found
        }


        maleCheckBox.setSelected(criteria.filterMale);
        femaleCheckBox.setSelected(criteria.filterFemale);

        // Ensure spinner values are within their model's actual bounds after model creation
        SpinnerNumberModel minModel = (SpinnerNumberModel) minAgeSpinner.getModel();
        minModel.setMinimum(initialDbMinAge); // Set actual min from data
        minAgeSpinner.setValue(Math.max((Integer)minModel.getMinimum(), Math.min(criteria.minAge, (Integer)minModel.getMaximum())));


        SpinnerNumberModel maxModel = (SpinnerNumberModel) maxAgeSpinner.getModel();
        maxModel.setMaximum(initialDbMaxAge + 20); // Set actual max from data + buffer
        maxAgeSpinner.setValue(Math.min((Integer)maxModel.getMaximum(), Math.max(criteria.maxAge, (Integer)maxModel.getMinimum())));
    }

    private void applyFilters() {
        currentFilterCriteria.filterFirstName = firstNameField.getText().trim();
        currentFilterCriteria.filterLastName = lastNameField.getText().trim();
        currentFilterCriteria.filterMiddleName = middleNameField.getText().trim();
        currentFilterCriteria.middleInitialOnly = middleInitialOnlyCheckBox.isSelected();
        currentFilterCriteria.filterGradeLevel = (String) gradeLevelComboBox.getSelectedItem();
        currentFilterCriteria.filterTrackStrand = (String) trackStrandComboBox.getSelectedItem();
        currentFilterCriteria.filterMale = maleCheckBox.isSelected();
        currentFilterCriteria.filterFemale = femaleCheckBox.isSelected();
        currentFilterCriteria.minAge = (Integer) minAgeSpinner.getValue();
        currentFilterCriteria.maxAge = (Integer) maxAgeSpinner.getValue();

        filtersApplied = true;
        setVisible(false);
        dispose(); // Important to release resources
    }

    private void clearFiltersAndApply() {
        // Reset fields in the dialog
        firstNameField.setText("");
        lastNameField.setText("");
        middleNameField.setText("");
        middleInitialOnlyCheckBox.setSelected(false);
        gradeLevelComboBox.setSelectedItem("All");
        trackStrandComboBox.setSelectedItem("All");
        maleCheckBox.setSelected(true);
        femaleCheckBox.setSelected(true);
        minAgeSpinner.setValue(initialDbMinAge);
        maxAgeSpinner.setValue(initialDbMaxAge + 5); // Default max for filter

        // Update the shared FilterCriteria object
        currentFilterCriteria.filterFirstName = "";
        currentFilterCriteria.filterLastName = "";
        currentFilterCriteria.filterMiddleName = "";
        currentFilterCriteria.middleInitialOnly = false;
        currentFilterCriteria.filterGradeLevel = "All";
        currentFilterCriteria.filterTrackStrand = "All";
        currentFilterCriteria.filterMale = true;
        currentFilterCriteria.filterFemale = true;
        currentFilterCriteria.minAge = initialDbMinAge;
        currentFilterCriteria.maxAge = initialDbMaxAge + 5;

        filtersApplied = true; // Indicate that filters (even cleared ones) were "applied"
        setVisible(false);
        dispose();
    }

    public boolean wereFiltersApplied() {
        return filtersApplied;
    }
    // No need for getFilterCriteria() as we are modifying the passed-in object directly
}


/**
 * StudentApp Class
 * Main application class using MigLayout and FlatLaf.
 */
public class StudentApp extends JFrame {
    private final DatabaseManager dbManager;
    private StudentTableModel tableModel;
    private JTable studentTable;
    private final FilterCriteria currentFilters; // Shared filter object

    private int currentPage = 1;
    private final int pageSize = 25; // Students per page
    private int totalPages = 1;

    private JLabel pageInfoLabel;
    private JButton prevButton, nextButton;
    private JTextField searchField;
    private JLabel appliedFiltersLabel;

    public StudentApp() {
        dbManager = new DatabaseManager();
        currentFilters = new FilterCriteria();
        int[] ages = dbManager.getMinMaxAge();
        currentFilters.minAge = ages[0]; // Initialize with actual data min
        currentFilters.maxAge = ages[1] + 5; // And actual data max + buffer

        setTitle("Student List Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Size will be set by pack() or specific dimensions later
        setLocationRelativeTo(null);

        initComponents();
        loadData();
        pack(); // Pack after components are added
        setMinimumSize(new Dimension(800, 600)); // Set a reasonable minimum size
        setLocationRelativeTo(null); // Center again after pack
    }

    private void initComponents() {
        // Main panel with MigLayout
        // Layout: "insets dialog, fill" - standard dialog insets, components fill available space
        // Columns: "[grow,fill]" - one column that grows and fills
        // Rows: "[]para[]para[grow,fill]para[]" - rows for top controls, table (grows), and bottom controls, with paragraph gaps
        JPanel mainPanel = new JPanel(new MigLayout("insets dialog, fill", "[grow,fill]", "[]para[]para[grow,fill]para[]"));
        mainPanel.setBorder(new EmptyBorder(5,5,5,5)); // Keep some outer padding if desired

        // --- Top Panel for Search and Filters ---
        // Using MigLayout for the top panel as well for finer control
        // "insets 0" - no extra padding inside this panel
        // Columns: multiple, for label, field, buttons, and label
        // Rows: single row
        JPanel topPanel = new JPanel(new MigLayout("insets 0, fillx",
                "[][grow,fill]para[]para[]para[right]", // label, search field, search btn, filter btn, applied filters label
                "[]")); // Single row

        topPanel.add(new JLabel("Search Name:"));
        searchField = new JTextField();
        topPanel.add(searchField, "growx"); // Search field grows horizontally

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            currentFilters.searchNameTerm = searchField.getText().trim();
            currentPage = 1;
            loadData();
        });
        topPanel.add(searchButton);

        JButton filterButton = new JButton("Advanced Filters...");
        filterButton.addActionListener(e -> openFilterDialog());
        topPanel.add(filterButton);

        appliedFiltersLabel = new JLabel("Filters: 0");
        topPanel.add(appliedFiltersLabel, "gapleft push"); // push to the right

        mainPanel.add(topPanel, "wrap"); // Add topPanel to mainPanel and wrap to next row

        // --- Center Panel: Table ---
        tableModel = new StudentTableModel(new ArrayList<>());
        studentTable = new JTable(tableModel);
        studentTable.setAutoCreateRowSorter(false);
        studentTable.setFillsViewportHeight(true);
        studentTable.setRowHeight(25); // Or use UIManager.getInt("Table.rowHeight") for L&F consistency
        // studentTable.setIntercellSpacing(new Dimension(0, 0)); // FlatLaf might handle this well

        // Column widths
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(110); // LRN
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(130); // Last Name
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(130); // First Name
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Middle
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Sex
        studentTable.getColumnModel().getColumn(5).setPreferredWidth(40);  // Age
        studentTable.getColumnModel().getColumn(6).setPreferredWidth(90);  // Grade
        studentTable.getColumnModel().getColumn(7).setPreferredWidth(70);  // Section
        studentTable.getColumnModel().getColumn(8).setPreferredWidth(150); // Track

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        studentTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Sex
        studentTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Age

        JScrollPane scrollPane = new JScrollPane(studentTable);
        mainPanel.add(scrollPane, "grow, wrap"); // Table scroll pane grows and fills, then wrap

        // --- Bottom Panel: Pagination ---
        // Using MigLayout for pagination as well
        // "insets 0, center" - no padding, components centered
        JPanel bottomPanel = new JPanel(new MigLayout("insets 0, center"));
        prevButton = new JButton("<< Previous");
        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadData();
            }
        });
        bottomPanel.add(prevButton);

        pageInfoLabel = new JLabel("Page 1 of 1");
        bottomPanel.add(pageInfoLabel, "gapx 15"); // Horizontal gap around page info

        nextButton = new JButton("Next >>");
        nextButton.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadData();
            }
        });
        bottomPanel.add(nextButton, "gapx 15");

        mainPanel.add(bottomPanel, "align center"); // Add bottomPanel, align it center

        setContentPane(mainPanel); // Set the main MigLayout panel as the content pane
    }

    private void loadData() {
        List<Student> studentsForPage = dbManager.getStudents(currentPage, pageSize, currentFilters);
        tableModel.setData(studentsForPage);

        int totalFilteredStudents = dbManager.getTotalStudentCount(currentFilters);
        totalPages = (int) Math.ceil((double) totalFilteredStudents / pageSize);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages && totalPages > 0) {
            currentPage = totalPages;
            studentsForPage = dbManager.getStudents(currentPage, pageSize, currentFilters); // Reload
            tableModel.setData(studentsForPage);
        } else if (totalFilteredStudents == 0 && currentPage != 1) {
             currentPage = 1; // Reset to page 1 if no results and not already on page 1
        }

        updatePaginationControls();
        updateAppliedFiltersLabel();
    }

    private void updatePaginationControls() {
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + " (Total: " + dbManager.getTotalStudentCount(currentFilters) + ")");
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    private void updateAppliedFiltersLabel() {
        int count = 0;
        // Check general search term
        if (currentFilters.searchNameTerm != null && !currentFilters.searchNameTerm.isEmpty()) count++;

        // Check detailed name filters
        if (currentFilters.filterFirstName != null && !currentFilters.filterFirstName.isEmpty()) count++;
        if (currentFilters.filterLastName != null && !currentFilters.filterLastName.isEmpty()) count++;
        if (currentFilters.filterMiddleName != null && !currentFilters.filterMiddleName.isEmpty()) count++;
        // Note: middleInitialOnly is a modifier, not a separate filter count item unless middleName is also set.

        // Check dropdown filters
        if (!"All".equals(currentFilters.filterGradeLevel)) count++;
        if (!"All".equals(currentFilters.filterTrackStrand)) count++;

        // Check sex filter (counts as 1 if not both selected)
        if (!currentFilters.filterMale || !currentFilters.filterFemale) { // If at least one is unchecked
            if (currentFilters.filterMale != currentFilters.filterFemale) { // And they are different (i.e., one selected, not none)
                count++;
            }
        }
        // If both male and female are false, it's a restrictive filter, but the logic above handles it.
        // If user unchecks both, it means "show no one by sex", which is a filter.
        // However, the current UI for FilterDialog defaults them to true and doesn't easily allow unchecking both to mean "no sex filter".
        // The current count logic is: if only one is selected, it's a filter. If both, no filter. If none (hypothetically), it's a filter.

        // Check age filter
        int[] dbAges = dbManager.getMinMaxAge(); // Get current min/max from data for comparison
        boolean minAgeDefault = currentFilters.minAge == dbAges[0];
        boolean maxAgeDefault = currentFilters.maxAge == (dbAges[1] + 5); // Compare with the default max used in FilterCriteria reset

        if (!minAgeDefault || !maxAgeDefault) {
            count++;
        }
        appliedFiltersLabel.setText("Filters: " + count);
    }


    private void openFilterDialog() {
        // Pass the *shared* currentFilters object to the dialog.
        // The dialog will modify this object directly.
        FilterDialog dialog = new FilterDialog(this, currentFilters, dbManager);
        dialog.setVisible(true); // This is a modal dialog, so code execution pauses here

        // After the dialog is closed (either by Apply, Clear, or Cancel):
        if (dialog.wereFiltersApplied()) {
            // Filters were changed (or cleared and then "applied")
            currentPage = 1; // Reset to first page
            loadData(); // Reload data with the modified currentFilters
        }
        // If dialog was cancelled, currentFilters remains as it was before opening.
    }

    public static void main(String[] args) {
        // It's crucial to set FlatLaf before any Swing components are created.
        try {
            FlatLightLaf.setup(); // Or FlatDarkLaf.setup(), FlatIntelliJLaf.setup(), etc.
            // For more control: UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
            // Fallback to system L&F or cross-platform L&F if FlatLaf fails
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            StudentApp app = new StudentApp();
            app.setVisible(true);
        });
    }
}
