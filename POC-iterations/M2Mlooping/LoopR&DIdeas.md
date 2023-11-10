November 10, 2023
R&D Discussion
Neil Beesley and Kip Twitchell

Michael Shapiro talked this week of a need for many to many processing capabilities.  Neil asked how we might satisfy this need in the most simple way.  So we spent an hour discussing it.

Objective: 
    - Smallest number of changes to existing tools
    - No required exits
  
Proposal:
    - Workbench changes:
      - New type of LR Index: Non unique key
      - New Logic Text Keyword:  Next Lookup {Join.ID}
    - MR91
      - New LUNX (replacing LUSM) to target LR
      - Perhaps an additional LKC before LUNX giving search mode
        - 0 = Start Search
        - 1 = Next Record
      - CFNX for Next Lookup 
        - true row pointing back up to LUNX
        - false row as the associated Join false row
    - MR95
      - Allow non-unique table load, removing binary search path build 
      - Model code
        - LUNX
        - CFNX

Sample Logic Table:
    RENX
    NV
    JOIN
    LKE....
    LKC = 0 for start search
    LUNX  
        - Initial call finds first row in table for partial key given
        - Branch not found if no match found (assuming beyond CFNX)
        - Set next record pointer to next record in table
    ....
    WR..
    CFNX
        - Change LKC = 1 to Next Record
        - Test pointer to next record for equal to lookup key
        - Test true, branch back to LUNX
        - Test False, fall through

Assumptions:
    - Each LKNX - CFNX operates a stack
    - Nested sets are nested loops