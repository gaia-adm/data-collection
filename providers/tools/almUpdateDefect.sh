#!/bin/bash

# script changes a selected impact multiple times in order to generate data in AUDIT tables for fetching it with alm-issue-change provider
# NOTE: set constants accordingly before running the script


#constants
almLocation=http://localhost:8082/qcbin
domain=DEFAULT
project=bp1
defectId=1
adminUser=sa
adminPassword=
regularUser=boris
loopCount=2

#login
lwsso=$(curl -i -X POST -H "Accept: application/json" -H "Content-Type: application/json" -d "<alm-authentication><user>"$adminUser"</user><password>"$adminPassword"</password></alm-authentication>" $almLocation/authentication-point/alm-authenticate | grep Set-Cookie | cut -d ':' -f2 | cut -d ';' -f1)
echo LWSSO:$lwsso

#create session
others=$(curl -i -X POST -H "Accept: application/json" -H "Content-Type: application/json" --cookie $lwsso -d "<session-parameters><client-type>Gaia ReST Client</client-type><time-out>60</time-out></session-parameters>" $almLocation/rest/site-session | grep 'QCSession\|ALM_USER\|XSRF-TOKEN' | sed -s 's/Set-Cookie: //g' | tr '\n' ';')
echo OTHERS:$others



##### update defect 1, total - 12 changes in every iteration

COUNTER=0
while [ $COUNTER -lt $loopCount ]; do
	echo -e "\n"===================== ITERATION $COUNTER
	# update severity
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"5-Urgent","owner":"sa"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"4-Very High","owner":"sa"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"3-High","owner":"sa"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"2-Medium","owner":"sa"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"1-Low","owner":"sa"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId

	# update owner
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"1-Low","owner":"'$regularUser'"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"1-Low","owner":"'$adminUser'"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId

	# update both severity and owner
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"5-Urgent","owner":"'$regularUser'"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"4-Very High","owner":"'$adminUser'"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"3-High","owner":"'$regularUser'"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"2-Medium","owner":"'$adminUser'"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId
	curl -i -X PUT -H "Accept: application/json; schema=alm-web" -H "Content-Type: application/json; schema=alm-web" --cookie $lwsso";"$others -d '{"entity":{"id":'$defectId',"severity":"1-Low","owner":"'$regularUser'"},"business-rules-validation-failure-level":"warning"}' $almLocation/rest/domains/$domain/projects/$project/defects/$defectId

	let COUNTER=COUNTER+1
	
done

echo -e "\n"FINISHED !!!
