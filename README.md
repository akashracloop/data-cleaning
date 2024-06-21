## Usage

### Clean CSV with Log

Endpoint to clean a CSV file:

```bash
curl --location 'http://localhost:9001/cleanCsvWithLog' \
--form 'file=@"/path/to/your/csv/file.csv"'


Endpoint to clean a CSV file and generate a change log:

```bash
curl --location 'http://localhost:9001/cleanCsv' \
--form 'file=@"/path/to/your/csv/file.csv"'
