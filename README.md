# Tiamat (Primordial Goddess of the Salty Sea)

The main object of this project is to learn some Android development while throwing an
app together that I might use.

Ultimately, I envision being able to record location/date/time/weather/amount caught/etc
at a fishing spot. Then at a future time, I would like to be able to draw a polygon
around an area I want to go fishing at and see any records that I previously recorded that the
polygon contains.

For example, let's say I go to Agnes lake one year at the beginning of June and have a
killer day around sunrise. A year later, maybe I go to brown's lake but don't hike up to Agnes,
and don't have a very good day fishing. A couple years later at the same time in June I'm feeling
going fishing in the Pioneer mountains but I'm not sure where. I draw a circle around the area,
and the app presents me with my records. I see that Browns lake isn't that great, but a hike
up to Agnes might be an awesome time.

Features that I want to learn:
  - ~~Get your location and zoom to it~~
  - ~~Permissions logic~~
  - ~~Persist coords (markers) locally~~
  - Provide additional information with the coords
    - present this information in the map
    - dynamic click on expand
  - Overlay a shape on the map
  - Draw a shape on the map
    - Save the shape
    - Identify markers inside of the shape
    - Identify if marker is within reasonable distance of shape border
  - Landing Screen
  - Hook up to an api (weather/fwp maybe?)
  - Toggle Satellite Layer
  - Toggle shapes layer
  - Orientate north button
  - Zoom to location button
  - scroll bar that effects current shapes
  - Backend with persistance
    - Share a record with a friend
    - Make a record public
    - see other public records