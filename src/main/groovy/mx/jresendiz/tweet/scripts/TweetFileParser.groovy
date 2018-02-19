package mx.jresendiz.tweet.scripts

import groovy.json.JsonSlurper

def list = []

new File(System.getenv("JSONL_FILE_LOCATION")).eachLine { line ->
    def object = new JsonSlurper().parseText(line)
    if (object.coordinates)
        list.add object
}

String finalSQL = "INSERT into tweets_information(tweet_id, username, lon, lat, geog) values \n"

List<String> sqlValues = []

list.each { tweetInfo ->
    List<Double> coordinates = tweetInfo.coordinates.coordinates
    String value = "('${tweetInfo.id_str}', '${tweetInfo.user.id}', ${coordinates[0]}, ${coordinates[1]}, ST_GeographyFromText('Point(${coordinates[0]} ${coordinates[1]})'))"
    sqlValues.add(value)
}

finalSQL += sqlValues.join(",\n") + ";"

def file = new File(System.getenv("SQL_FILE_OUTPUT"))
file.write finalSQL
return null
