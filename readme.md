# Reportable Workflow

## General
This module is an alternative to the BasicWorkflows and provides a separate report for the editorial support for the actions Release and Delete. By using the executables included in the module in a corresponding workflow, an element is validated for the respective action and, if appropriate, the report is opened to provide the editor with more detailed information or jump-off mechanisms for the respective action-preventing elements. An action-independent validation via can also be done outside the workflow via the report.

Project specific expertise will not be reflected by adjustments in this module. In order to map this and thus to influence the respective logics, specific handlers for individual FS objects or a complete controller can be implemented in an additional module. A code-side integration in this module is not necessary. Just the implementation of specific interfaces or the derivation of provided classes in combination with settings in the ProjectApp of this module are sufficient.


## Use in the project
After installing the module in the ServerManager, the report functionality and the validation capability are technically available everywhere, depending on the selected action (`RELEASE` or `DELETE`). In order to gain the functionality of the reports explicitly in a project, the corresponding ProjectApp has to be added to the project. The ProjectApp is on the one hand the sign in which project the report should be displayed. On the other hand, in the configuration the use of the HandlerController, or the handlers, as well as the activation of the report can be configured for each type of action.
Do not forget to add the WebApp to your project, if you also want to use the report functionality in the ContentCreator.


## Public componets
| Component | technical name |
| --- | --- |
| ValidateElementExecutable | reportable_workflow_validate_release_element_executable bla |
| ReleaseElementExecutable | reportable_workflow_release_element_executable |
| ValidateDeleteElementExecutable | reportable_workflow_validate_delete_element_executable |
| DeleteElementExecutable | reportable_workflow_delete_element_executable |
| ReleaseValidationStateDataAccessPlugin | reportable_workflow_release_validation_state_data_access_plugin |
| DeleteValidationStateDataAccessPlugin | reportable_workflow_delete_validation_state_data_access_plugin |
| JavaClientSession | reportable_workflow_java_client_session |
| WebEditSession | reportable_workflow_web_edit_session |
| ReportableWorkflowProjectApp | reportable_workflow_projectapp |
| WebApp | reportable_workflow_web_app |

## Interfaces and abstract classes
The following abstract class and handlers can be implemented or derived to represent the project-specific expertise.

```java
Interface: com.espirit.ps.rw.dependency.Handler
```
The handler interface offers exactly one method: ```execute (Handle, Manager)```. The handle is a representation of the current item while the manager provides additional information, e.g. The currently used logic (```RELEASE``` or ```DELETE```). If a page is to be manipulated, it will be in the handle. The handle can now accept the result of the evaluation in the form of a ValidationState. In addition, you can add elements to Checker as ```nextHandle()```. Control of the execution of each handler is the responsibility of the controller. The manager is responsible for the correct initialization of the controller and associated handlers.

```java
Class: com.espirit.ps.rw.dependency.AbstractDefaultHandler
```
The AbstractDefaultHandler already implements the interface Handler and also already provides methods, e.g. for checking IncomingReferences or validation of data. These methods are accessible but not overwritable (`final`).

```java
Interface: com.espirit.ps.rw.dependency.HandlerController
```
The HandlerController is responsible for controlling the individual trading. He gradually delivers all registered handlers to the manager who calls them. In addition, the controller offers several methods that are queried at different times within the control flow (listener principle).

```java
Class: com.espirit.ps.rw.dependency.AbstractHandlerController
```
The AbstractHandlerController already implements the interface HandlerController and also already provides methods, e.g. for add handlers. These methods are accessible but not all overwritable (`final`).

```java
Class: com.espirit.ps.rw.executable.AbstractExecutable
```
The class AbstractExecutable can be implemented in a project-specific environment and already provides all necessary methods. Only the medthode execute () must then be implemented. Basically, the use of these calices is not necessary. For a self-implementation of an executable, only the call to the actual processing is relevant:
`ReportableWorkflow.execute(BaseContext, StoreElement, Boolean, Manager.Action)`.

```
## Bamboo (currently not active)
* [Master plan](https://ci.e-spirit.de/browse/PSSOLUTIONSREWO-DEFAULTMASTER)
* [Feature plan](https://ci.e-spirit.de/browse/PSSOLUTIONSREWO-DEFAULTFEATURE)
* [Release plan](https://ci.e-spirit.de/browse/PSSOLUTIONSREWO-DEFAULTRELEASE)
```

## Currently Involved
| Name | Role | e-mail |
| ---- | ------ | ----- |
| Stefanie Drerup | Consultant | drerup@e-spirit.com |

Former members have been Mario Vaccarisi, Armin Wolf, Florian Ostertag.