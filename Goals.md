# Goals of STEP2SysMLv2

We are going to build a capability to import and export STEP format to/from SysMLv2 using the geometry of SysMLv2/KerML.

## primary goals

- import STEP geometry to SysMLv2
- export SysMLv2 geometry to STEP
- Written in Groovy script to be run in CATIA Magic
- Hides complexities of geometry  from the user and SysML modelers
- Enable core capabilities that a systems engineer would want from STEP geometry
-- Calculations related to volume
--- Mass
--- Center of mass
--- Moments of inertia
--- Center of pressure
--- Center of volume
--- Spacing of holes
--- etc.
- Generate requirements from annotations ( PMI)
- generate requirements from geometry

## secondary goals

- keep geometry semantics aligned with SysMLv2/KerML
- keep geometry semantics aligned with STEP
- make sure the transformation preserves the semantics of the geometry (including some tagging in SysMLv2 to aid in round trip usage).

## Use Cases

- Import from STEP to SysMLv2:
  - import the geometry into a SysMLv2 model via the API
  - Augment the geometry with metadata to allow sync or data not supported by SysML directly
  - Add additional data to allow volume, mass, and other data to be computed (perhaps witht a part that has the geometry)
  - Part breakdown of an assembly or collection of non-intersecting volumes to allow individual mass/volume calcuations on the parts.

- Generate requirements based on 3D Annotations and other PMI
  - Generate requirements based on shapes in the STEP
  - Report import statistics (number of shapes, volume, surface area, center of mass, moments of inertia, mass properties, moving/translating, scaling, etc.)
  - Aid user to name parts of the assembly
  -
- Draw geometry in the 3D viewer of the IDE
  - Draw the geometry in the 3D viewer of CATIA Magic

### Wishlist

- library of additional calcualtions on the geometry
-- volume, surface area, center of mass, moments of inertia, mass properties, moving/translating, scaling, etc.
-- Export/import to other modeling tools of STEP via their API
-- support for 2D and 3D geometry
-- support for annotations to the geometry

- support for annotations to the geometry
--
- support for Product Manufacturing Information (PMI)
-- import PMI to SysMLv2
-- export PMI from SysMLv2
- Translation of shapes to requirements
- Translation of dimensional requirements to geometric constraints
- Validation of shapes against requirements
-- including mass and inertia properties

## Instructions for development

- We will build a small example that imports a STEP file and computes the volume and mass of the geometry. This will be used to evaluate the feasibility of the project.
- We should use test harnesses to allow the LLM or AI tool to develop and debug the tool. This will allow us to evaluate the feasibility of the project.
- We should use the SysMLv2 Validator to to test that the correct sysml code is produced.
- We need to research and use a good open source tool for the STEP file validation and creation.
- We should also research the geometry aspect of the SysMLv2 and ensure that CATIA Magic (also known as Cameo, MagicDraw, MD, and other product bundles names) supports the geometry aspect of the SysMLv2.
- This is a test first methodology project to ensure that the code produced is always validated before testing the integration.
- use best practicess for Groovy script development and testing
- use best practicess for Java Exception handling and error reporting (errors echoed to the MD console).
- Look at avaialable skinlls and assets in
-- submodules/SysMLv2CheatSheet
-- submodules/sysml-validator
-- submodules/TutorialForCatiaMagicApiMCP

## Reference Documents

### STEP Language & Standards

| Document | Description |
|---|---|
| ISO 10303-1 | STEP Overview — Product Data Representation and Exchange |
| ISO 10303-11 | EXPRESS language (the schema language for STEP) |
| ISO 10303-21 | Part 21 — Clear Text Encoding (the `.stp` file format) |
| ISO 10303-242 | AP242 — Managed Model-Based 3D Engineering (GD&T, PMI, tessellated geometry) |
| NIST STEP File Analyzer | Validation and analysis of STEP files |

### STEP Open Source Tools Analysis (Java / Groovy Focus)

Given the constraint of running inside CATIA Magic (which strongly prefers pure Java libraries to avoid native JNI crashes) and the rejection of JSDAI (AGPL/Eclipse dual license), the landscape of open-source STEP parsers is as follows:

| Tool | Language | License | Viability for this Project |
|---|---|---|---|
| **JSDAI** | Java | AGPL-3.0 | **Rejected**: Commercial/AGPL dual-license trap; heavy Eclipse dependencies; outdated. |
| **OpenCASCADE (OCCT)** | C++ | LGPL-2.1 | **Low**: The industry standard for AP242, but requires JNI wrappers (like jCAE) and deploying native binaries (DLLs/.so) inside Cameo, which is brittle and hard to distribute. |
| **STEPcode** | C++ / Python | BSD | **Low**: Excellent tool but no native Java bindings available. |
| **AlexFemec/STEP-file-parser** | Java | MIT | **Medium/High**: A lightweight, community-built pure Java parser for Part 21 files. Good starting point for tokenizing, but lacks full AP242 geometry evaluation. |
| **BIMserver Part21Parser** | Java | GPL/MIT | **Medium**: Part of a larger IFC/BIM server; parses STEP structure but is heavily tied to IFC schemas. |
| **Custom Text/Regex Parser** | Java/Groovy | N/A | **High**: Since we only need a small subset of AP242 (Tessellated shapes, PMI annotations, assembly tree), a targeted pure-text parser (like the `StubStepLibraryAdapter` we started) avoids all licensing, dependency, and native-binary headaches. |

> **Recommended Path Forward:** We will build out our custom pure-Groovy lightweight parser to handle the exact AP242 entities we need (Tessellated geometry, Assemblies, PMI). It is currently passing tests and avoids all licensing issues. If the parsing gets too complex, we will evaluate adopting the MIT-licensed `AlexFemec/STEP-file-parser` as the base parser engine.

### SysMLv2 Geometry

| Resource | Location / Link |
|---|---|
| SysMLv2 Geometry Domain Library | `sysml.library/Domain Libraries/Geometry` in Systems-Modeling/SysML-v2-Release |
| KerML Specification (OMG) | <https://www.omg.org/spec/KerML> |
| SysMLv2 CheatSheet (submodule) | `submodules/SysMLv2CheatSheet` |

### SysMLv2 Tools & Validator

| Tool | Notes |
|---|---|
| CATIA Magic / Cameo (2026xR1) | Primary target IDE; Groovy scripts via Open API |
| SysMLv2 Validator (submodule) | `submodules/sysml-validator` |

### Groovy Scripting & CATIA Magic API

| Resource | Notes |
|---|---|
| TutorialForCatiaMagicApiMCP (submodule) | `submodules/TutorialForCatiaMagicApiMCP` — scripts, best practices, test harness |
| MagicDraw Groovy Gotchas KI | Antigravity knowledge item: `magicdraw_groovy_gotchas` |
| `SessionManager` | Wrap all model mutations in session: `createSession` / `closeSession` |
| `Finder` | Use `Finder.byQualifiedName()`, `Finder.byTypeRecursively()` |
