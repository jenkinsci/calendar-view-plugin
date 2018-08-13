# Test cases for events

### Points in time

* now: current date and time
* start: inclusive beginning of selection range
* end: exclusive end of selection range

### Event types:

* `[#####]`: Finished event that should be included
* `{#####}`: Finished event that should not be included
* `[*****]`: Running event that should be included
* `{*****}`: Running event that should not be included
* `[=====]`: Scheduled event that should be included
* `{=====}`: Scheduled event that should not be included

## Case 1: now < start < end

<pre>
--- time ------------------------------>

           :       |                      |
{#####} 1  :       |                      |
           :       |                      |
   {*********} 2   |                      |
           :       |                      |
      {************} 3                    |
           :       |                      |
        [*************] 4                 |
           :       |                      |
           : {===} 5                      |
           :       |                      |           
           : {=====} 6                    |
           :       |                      |
           :  [===========] 7             |
           :       |                      |
           :       [===========] 8        |
           :       |                      |
           :       |       [=========] 9  |
           :       |                      |
           :       |                [===========] 10
           :       |                      |
           :       |                      {===========} 11
           :       |                      |
           :       |                      |      {===========} 12
           :       |                      |
          now    start                   end
</pre>

Includes:
* `4`: Currently running events estimated to end in the selection range
  * `running(start, end)`
* `7`: Scheduled events estimated to end in the selection range
  * `scheduled-backwards(start, end)`
* `8`,`9`,`10`: Scheduled events that start within the selection range
  * `scheduled-forwards(start, end)`
  

Does not include:
* `1`: All finished events (since they cannot have finished in the future and the selection range is entirely in the future)
* `2`,`3`: Currently running events that start before the selection range but are not estimated to end within the selection range
* `5`,`6`: Scheduled events that start before the selection range but are not estimated to end within the selection range
* `11`,`12`: Scheduled events that start after the selection range
