# phenogen-pipeline

Maintains PhenoGen cross-reference IDs in RGD for rat and mouse genes.

## Overview

For each active rat and mouse gene in RGD, creates an XDB ID entry (xdb_key=51)
using the gene symbol as the accession ID. Syncs these entries against the database:
inserts new, deletes obsolete, and updates timestamps on matching records.

## Logic

For each species (rat, mouse):
1. Load existing PhenoGen XDB IDs from RGD
2. Generate incoming IDs from all active genes (gene symbol as accession)
3. Insert new IDs not yet in RGD
4. Delete IDs no longer supported by active genes
5. Update modification date on matching IDs

## Logging

- `status` — pipeline progress and summary counters per species

## Build and run

Requires Java 17. Built with Gradle:
```
./gradlew clean assembleDist
```
