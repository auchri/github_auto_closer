# GitHub Auto Closer
Closes inactive issues on [GitHub](https://github.com). [![Build Status](https://travis-ci.org/auchri/github_auto_closer.svg?branch=master)](https://travis-ci.org/auchri/github_auto_closer)

Preview:
![Preview of an closed issue](https://github.com/auchri/github_auto_closer/blob/develop/docs/preview.png)

## Usage
Get the jar file (from the [releases page](https://github.com/auchri/github_auto_closer/releases) or by [building](https://github.com/auchri/github_auto_closer/blob/develop/README.md#building) the program by yourself.

Obtain a OAuth token from [GitHub](https://github.com/settings/tokens). The token needs at least the scope `public_repo` and the user of the token must have `write` rights for the target repository.

Then execute the jar: `java -jar github_auto_closer_1.0.jar -o oauth_key -n namespace -r repository -d 360`
(Note that Java 7 is required)

### Available parameters
| Short | Long        | Description                                                                    | Required |
|-------|-------------|--------------------------------------------------------------------------------|----------|
| x     | debug       | Enables the debug mode                                                         | No       |
| o     | oauth_token | OAuth token from OAuth token from [GitHub](https://github.com/settings/tokens) | Yes      |
| n     | namespace   | Namespace of the repository (e.g. `foo/bar` → namespace is `foo`)              | Yes      |
| r     | repository  | Name of the repository in the namespace (e.g. `foo/bar` → repository is `bar`) | Yes      |
| d     | days        | Amount of days after which an inactive issue should be closed                  | Yes      |
| p     | include_pr  | If inactive pull requests should be also closed                                | No       |
| l     | label       | Label to add the the closed issues                                             | No       |

## Building

Navigate to the directory containing the project files and execute `mvn install`.
