# STEP2SysMLv2

STEP2SysMLv2 is an engineering tool designed to bridge the gap between 3D CAD geometry (STEP AP242/AP203 formats) and systems engineering models (SysML v2 / KerML). The project enables importing STEP geometry, calculating mass properties (volume, mass, center of gravity, moments of inertia), extracting Product Manufacturing Information (PMI) annotations, and exporting geometric models back to STEP format from SysML v2.

---

## 📂 Project Structure

This repository is organized to separate core application logic, MagicDraw/Cameo integrations, web visualization, and development scratchpads:

```
├── .agents/                 # AI/LLM Agent configuration (rules and workflows)
├── bin/                     # Gradle compilation outputs
├── build/                   # Gradle build outputs
├── build.gradle             # Main Gradle build configuration
├── Goals.md                 # Project roadmap, requirements, and reference standards
├── gradle/                  # Gradle wrapper files
├── scratch/                 # LLM & developer scratchpad (temporary scripts and tests)
├── scripts/                 # Groovy scripts for CATIA Magic / Cameo Open API
│   ├── Step2SysMLImporter.groovy # Core script to import STEP data into Cameo
│   └── export_to_graphify.groovy # Script to export Cameo models to knowledge graphs
├── settings.gradle          # Gradle project settings
├── src/                     # Core Groovy and Java source code
│   ├── main/groovy/geometry # Bounding box, inertia, and mass calculators
│   ├── main/groovy/server   # ViewerServer backend (JVM implementation)
│   └── main/groovy/step     # Custom pure-Groovy lightweight STEP AP242 parser
├── submodules/              # Managed external Git dependencies
│   ├── SysMLv2CheatSheet    # Local copy of SysML v2 syntax cheat sheet
│   ├── sysml-validator      # Local copy of SysML v2 syntax validator
│   └── TutorialForCatiaMagicApiMCP # Reference tutorials and Cameo API scripts
├── sysml_to_step.py         # Python utility to convert SysML v2 polyhedrons to STEP format
├── test/                    # JUnit and Spock unit tests
├── test-data/               # Sample STEP files (.stp) and exported SysML v2 files
├── viewer/                  # 3D Web Viewer frontend (three.js and occt-import-js)
├── viewer_server.py         # Python-based web server for the 3D Viewer
└── web-editor/              # React-based web editor components
```

---

## 🚀 Setup & Usage Guide

### 1. Running Unit Tests
The project uses the **Spock Framework** for testing Groovy and Java code.
To run the automated tests:
```bash
./gradlew test
```
Test reports will be generated at `build/reports/tests/test/index.html`.

### 2. Launching the 3D Viewer
The 3D viewer allows you to inspect STEP files and visualize them in a web browser using Three.js and WebAssembly-based STEP parsing.
To start the viewer server:
```bash
python viewer_server.py
```
Then, open your browser and navigate to:
👉 **[http://localhost:8080/viewer/index.html](http://localhost:8080/viewer/index.html)**

### 3. Converting SysML v2 Polyhedrons to STEP
If you have generated SysML v2 geometric code containing polyhedrons, you can convert them back to a physical STEP file using the Python utility:
```bash
python sysml_to_step.py <path_to_input.sysml> <path_to_output.step>
```

### 4. Running the Importer in CATIA Magic / Cameo Systems Modeler
1. Open your model in Cameo / MagicDraw.
2. Open the **Groovy Console** (`Tools` -> `Macros` -> `Groovy Console`).
3. Load and execute `scripts/Step2SysMLImporter.groovy`.
4. The script parses your STEP file and creates the corresponding SysML v2 representation via the Cameo Open API.

---

## 🤖 AI & LLM Agent Guidelines

This section provides instructions for AI coding assistants (like Gemini and Claude) working on this codebase.

### 📝 Scratchpad & Testing
- **Do not clutter the root directory** with temporary test files, python scripts, or experimental code.
- All temporary scripts, experimental parsers, regex tests, and one-off execution files **MUST** be placed inside the `scratch/` directory.
- The `scratch/` directory is tracked by Git so that your experimental workflows are preserved and visible to the user.

### 🛠️ Using Submodules for Development
The project contains three submodules under `submodules/` that act as essential reference materials:
1. **`submodules/sysml-validator`**: Use the validator CLI or Cameo test harness to validate any generated SysML v2 code for syntax correctness before proposing changes.
2. **`submodules/SysMLv2CheatSheet`**: Consult this directory for correct SysML v2 syntax patterns, structural definitions, and geometry domain libraries.
3. **`submodules/TutorialForCatiaMagicApiMCP`**: Consult this directory for Cameo Open API scripting examples, session management rules, and best practices.

### 🤖 Agent Configurations
- **`.agents/rules/graphify.md`** contains rules for mapping the codebase architecture.
- **`.agents/workflows/graphify.md`** contains persistent workflows for updating the project's knowledge graph.
- These files are committed to the Git repository to ensure that agent behaviors and instructions remain consistent across all sessions.
