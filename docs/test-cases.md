# Test cases for build inclusion

### Points in time

* now: current date and time
* start: inclusive beginning of selection range
* end: exclusive end of selection range

### Event types:

* `[#####]`: Finished build that should be included
* `{#####}`: Finished build that should not be included
* `[*****]`: Running build that should be included
* `{*****}`: Running build that should not be included
* `[=====]`: Scheduled build that should be included
* `{=====}`: Scheduled build that should not be included

## Case 1: now < start < end

```
--- time --------------------------------------------------------->
           :       |                      |
{#####} 1  :       |                      |
           :       |                      |
   {*********} 2   |                      |
           :       |                      |
      {************} 3                    |
           :       |                      |
        [*************] 4                 |
           :       |                      |
           {***} 5 |                      |
           :       |                      |
           [**********] 6                 |
           :       |                      |           
           : {===} 7                      |
           :       |                      |           
           : {=====} 8                    |
           :       |                      |
           :  [===========] 9             |
           :       |                      |
           :       [===========] 10       |
           :       |                      |
           :       |       [=========] 11 |
           :       |                      |
           :       |                [===========] 12
           :       |                      |
           :       |                      {===========} 13
           :       |                      |
           :       |                      |      {===========} 14
           :       |                      |
          now    start                   end
```

Includes:
* `4`,`6`: Running builds estimated to end in the selection range
  * `running(start, end)`
* `9`: Scheduled builds that start before the selection range but are estimated to end in the selection range
  * `scheduled-backwards(now + 1min, start)`
* `10`,`11`,`12`: Scheduled builds that start within the selection range
  * `scheduled-forwards(start, end)`

Does not include:
* `1`: All finished builds (since they cannot have finished in the future and the selection range is entirely in the future)
* `2`,`3`,`5`: Running builds that start before the selection range but are not estimated to end within the selection range
* `5`,`7`,`8`: Scheduled builds that start before the selection range but are not estimated to end within the selection range
* `13`,`14`: Scheduled builds that start after the selection range


## Case 2: now = start < end

```
--- time --------------------------------------------------------->
           !                              |
{#####} 1  !                              |
           !                              |
     {#####} 2                            |
           !                              |
   {*******} 3                            |
           !                              |
      [**********] 4                      |
           !                              |
           [**********] 5                 |
           !                              |
           !          [===========] 6     |
           !                              |
           !                        [===========] 7
           !                              |
           !                              {===========} 8
           !                              |
           !                              |      {===========} 9
           !                              |
         start                           end
          now
```

Includes:
* `4`,`5`: Running builds estimated to end in the selection range
  * `running(start, end)`
* `6`,`7`: Scheduled builds that start within the selection range
  * `scheduled-forwards(now + 1min, end)`
  
Does not include:
* `1`: All finished builds (since they cannot have finished in the selection range)
* `3`: Running builds that start before the selection range but are not estimated to end within the selection range
* `8`,`9`: Scheduled builds that start after the selection range

## Case 4: start < end = now

```
--- time --------------------------------------------------------->
           |                               !
{#####} 1  |                               !
           |                               !
     {#####} 2                             !
           |                               !
        [#####] 3                          !
           |                               !
           [########] 4                    !
           |                               !
           |           [########] 5        !
           |                               !
           |                           [*******] 6
           |                               !           
           |                               {*******} 7 
           |                               !
           |                               !    {========} 8
           |                               !
           |                               !
         start                            end
                                          now
```

Includes:
* `3`,`4`,`5`: Finished builds that start or end in the selection range
  * `finished(start, end)`
* `6`: Running builds started in the selection range
  * `running(start, now)`
  
Does not include:
* `1`,`2`: Finished builds that don't start or end in selection range
* `7`,`8`: Scheduled builds that start after the selection range


