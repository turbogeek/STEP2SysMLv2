const fs = require('fs');

class StepExporter {
    constructor() {
        this.entityCount = 10;
        this.lines = [];
    }

    nextId() {
        return this.entityCount++;
    }

    exportScene(numParts, filename) {
        this.lines = [];
        this.entityCount = 10;
        this.lines.push(`ISO-10303-21;`);
        this.lines.push(`HEADER;`);
        this.lines.push(`FILE_DESCRIPTION(('Performance Test Export'),'2;1');`);
        const dateStr = new Date().toISOString().substring(0, 19);
        this.lines.push(`FILE_NAME('${filename}','${dateStr}',('TestBot'),('Company'),'preprocessor','system','authorization');`);
        this.lines.push(`FILE_SCHEMA(('AP242_MANAGED_MODEL_BASED_3D_ENGINEERING_MIM_LF { 1 0 10303 442 1 1 4 }'));`);
        this.lines.push(`ENDSEC;`);
        this.lines.push(`DATA;`);
        
        // Dummy contexts
        this.lines.push(`#1 = PRODUCT_CONTEXT('','',#0);`);
        this.lines.push(`#2 = PRODUCT_DEFINITION_CONTEXT('part definition',#0,'design');`);

        // Root Assembly
        const rootProdId = this.nextId();
        this.lines.push(`#${rootProdId} = PRODUCT('Root_Assy','Test Scene','',(#1));`);
        const rootPdfId = this.nextId();
        this.lines.push(`#${rootPdfId} = PRODUCT_DEFINITION_FORMATION('1.0','Initial Release',#${rootProdId});`);
        const rootPdId = this.nextId();
        this.lines.push(`#${rootPdId} = PRODUCT_DEFINITION('design','',#${rootPdfId},#2);`);

        for (let i = 0; i < numParts; i++) {
            this.exportNode(`Part_${i}`, rootPdId, i);
        }

        this.lines.push(`ENDSEC;`);
        this.lines.push(`END-ISO-10303-21;`);
        
        fs.writeFileSync(filename, this.lines.join('\n'));
        console.log(`Generated ${filename} with ${numParts} parts. Total entities: ${this.entityCount}`);
    }

    exportNode(name, parentPdId, index) {
        // Create Product
        const prodId = this.nextId();
        this.lines.push(`#${prodId} = PRODUCT('${name}','TestPart','',(#1));`);
        const pdfId = this.nextId();
        this.lines.push(`#${pdfId} = PRODUCT_DEFINITION_FORMATION('1.0','Initial Release',#${prodId});`);
        const pdId = this.nextId();
        this.lines.push(`#${pdId} = PRODUCT_DEFINITION('design','',#${pdfId},#2);`);

        // Link to Parent Assembly
        if (parentPdId) {
            const nauoId = this.nextId();
            this.lines.push(`#${nauoId} = NEXT_ASSEMBLY_USAGE_OCCURRENCE('NAUO','Instance',#${parentPdId},#${pdId},$);`);
        }

        // Output geometry as a single point to simulate data presence
        const ptId = this.nextId();
        this.lines.push(`#${ptId} = CARTESIAN_POINT('${name}_Center',(${index.toFixed(4)},0.0000,0.0000));`);
        
        // We'll also add an ADVANCED_BREP_SHAPE_REPRESENTATION just to make OCCT attempt to parse it as a shape if it traces it.
        // It's invalid without topology but it adds parsing overhead.
        const repId = this.nextId();
        this.lines.push(`#${repId} = ADVANCED_BREP_SHAPE_REPRESENTATION('','',(#${ptId}),#0);`);
    }
}

const exporter = new StepExporter();
exporter.exportScene(10, '../test-data/test-10.stp');
exporter.exportScene(100, '../test-data/test-100.stp');
exporter.exportScene(1000, '../test-data/test-1000.stp');
exporter.exportScene(10000, '../test-data/test-10000.stp');
