# openml-rdf
Integrating OpenML with Linked Data.

## Install

```bash
mvn compile package
```

## Example usage

### Build vocabulary

```bash
./build-vocabulary.sh etc/spec.txt etc/OpenML.rdf etc/OpenML_out.rdf
```

### RDFize OpenML entity

```bash
./rdfize.sh Task 3573
```

### Generate all RDF files for a given class

```bash
sh register-api-key.sh <OPENMLAPIKEY>
./populate.sh Task
```
