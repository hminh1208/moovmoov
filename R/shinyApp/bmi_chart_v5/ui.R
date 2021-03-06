# bmi chart v5

# load libraries
library(shiny)
library(plotly)
library(RMySQL) 
library(shinycssloaders)
library(shinydashboard)
library(shinyBS)
library(reshape2)
library(shinyWidgets)

# Define UI for application shiny dashboard
shinyUI(fluidPage(
 
  tags$style(HTML("@import url('https://fonts.googleapis.com/css?family=Poppins:300');")),
  tags$style(HTML("body {font-family: 'Poppins', sans-serif; line-height: 1.5;}")),
  tags$style(HTML("h2 {font-family: 'Poppins', sans-serif; line-height: 1.5;}")),
  tags$style(HTML("h3 {font-family: 'Poppins', sans-serif; line-height: 1.2; font-weight: 100}")),
  tags$style(HTML("p {font-family: 'Poppins', sans-serif; line-height: 1.5; font-weight: 300}")),
  
  dashboardPage(
    dashboardHeader(titleWidth=200,disable=TRUE
      ),
    dashboardSidebar(width=180, collapsed=TRUE,
      sidebarMenu(id="tabs",
        menuItem("Results", tabName = "results", icon = icon("plus-square")),
        menuItem("Children's Nutrition", tabName="diet", icon=icon("cutlery")), 
        menuItem("Victorian Analysis", tabName = "vic", icon = icon("line-chart")),
        menuItem("Physical Activity", tabName = "pa", icon = icon("soccer-ball-o"))
      )
    ),
    dashboardBody(
      tags$head(
        tags$style(HTML(".content-wrapper {background-color: #ffffff;}
                        .left-side, .main-sidebar{padding-top: 30px;}
                        .skin-blue .main-sidebar {background-color: #ffffff;}
                        .skin-blue .main-sidebar .sidebar .sidebar-menu .active a{background-color: #fed136;}
                        .skin-blue .sidebar-menu>li.active>a, .skin-blue .sidebar-menu>li:hover>a {color: #23292c; background: #f1f0eb; border-left-color: #ff70bb;}
                        .skin-blue .sidebar-menu>li>a {border-left: 3px solid #b3b3b3c2;}
                        .skin-blue .sidebar a {color: #23292c;}
                        .small-box { background-color: #ffffff !important; color: #274C77 !important; }"))
        #tags$style(".small-box { background-color: #ffffff !important; color: #274C77 !important; }")
      ),
      actionBttn("results_bttn","Results", size='sm'), 
      actionBttn("diet_bttn","Children's Nutrition", size='sm'), 
      actionBttn("vic_bttn","Victorian Analysis", size='sm'),
      actionBttn("pa_bttn","Physical Activity", size='sm'),
      hr(),
      tabItems(
        
        # results tab
        tabItem("results", h3("Results"), 
                
                fluidRow(
                  column(width=6, offset=3,
                         valueBoxOutput("age"),
                         valueBoxOutput("weight"),
                         valueBoxOutput("height")
                         )
                  
                ),
                hr(),
                fluidRow(
                  column(width=5, style="margin-top: -2rem;", offset=1,
                         fluidRow(
                           withSpinner(plotlyOutput("percentile", width="auto", height=180))
                         ),
                         hr(),
                         fluidRow(
                           infoBoxOutput("bmi_text", width=6),
                           bsModal("bmi_explain",
                                   "What is BMI?","btn",
                                   p("Body mass index is a calculation that uses height and weight to estimate body fat."),
                                   p("Too much body fat is a problem because it can lead to illnesses and other health problems."),
                                   p("Even though BMI may not always tell the whole story, 
                                     but it is often a fast and easy way to check on how your kid is growing."),
                                   p("Once you know your child's BMI, you'll be able to find which category he or she falls in."),
                                   p("(Shown in the bar chart)")
                                   ),
                           infoBoxOutput("percentile_text", width=6),
                           bsModal("percentile_explain",
                                   "Why Percentiles Are Not Like the Grades in School","btn",
                                   p("Sometimes it can be confusing when you first see the word 'percentile'.", tags$br(),
                                     "Out of habit, we tend to think the higher percentile the better."),
                                   p("To give an example why this is not the case here: "),
                                   p("If your child's BMI is at the 90th percentile line, 
                                     that means that out of 100 normal children their age,
                                     he or she has a higher BMI than 90 children and lower than only 10."),
                                   p(tags$a(href="http://www.education.vic.gov.au/childhood/professionals/health/Pages/bmicalculate.aspx",
                                            "Click here for more on interpreting BMI percentiles", target="_blank"))
                                   )
                           
                         ),
                         hr(),
                         h4("Click on above boxes to learn more", style="text-align: center;")
                         ),
                  column(width=5, style="padding-right: 5rem;",
                         infoBoxOutput("summary", width=NULL))
                  # column(width=3, align="center",
                  #        #plotlyOutput("chart", width="auto", height=520)
                  #        withSpinner(plotlyOutput("percentile", width=250, height="auto"))
                  #        ),
                  # column(width=3, 
                  #        fluidRow(
                  #          infoBoxOutput("percentile_text", width=NULL)
                  #          
                  #        ),
                  #        tags$br(),
                  #        fluidRow(
                  #          infoBoxOutput("bmi_text", width=NULL),
                  #          bsTooltip(id="bmi_text",
                  #                    title=paste("BMI is a measure used to determine if you are at a healthy weight for your height"),
                  #                    placement = "bottom", trigger = "hover",
                  #                    options = NULL)
                  #        )
                  #        ),
                  # column(width=5, style="margin-left:4rem",
                  #        fluidRow(
                  #          infoBoxOutput("summary", width=NULL)
                  #        )
                  #        )
                )
                ),
        
        # diet tab
        tabItem("diet", h3("How many serves of each food group should my child be getting daily?"), tags$br(),
                fluidRow(
                  column(width=4, offset=1,
                         box(title="Find out now!", status="warning", width=NULL,
                             selectInput("age_group", label="Select age group:",
                                         choices=c("Age 2 to 3"="2-3",
                                                   "Age 4 to 8"="4-8",
                                                   "Age 9 to 11"="9-11",
                                                   "Age 12 to 13"="12-13",
                                                   "Age 14 to 18"="14-18")),
                             radioButtons("gender", label="Select gender:",
                                          choices=c("Boy"="boy",
                                                    "Girl"="girl")),
                             p("Data from ", tags$a(href="https://www.eatforhealth.gov.au/food-essentials/how-much-do-we-need-each-day/recommended-number-serves-children-adolescents-and",
                                                    target="_blank", "eatforhealth.gov.au"))
                            ),
                         infoBoxOutput("serves", width=NULL)
                         ),
                  column(width=6, style="padding-right: 4rem;",
                         box(title="", status="warning", width=NULL,
                             withSpinner(plotlyOutput("pie"))),
                         box(title="Additional Dietary Links", status="warning", width=NULL, collapsible = TRUE, collapsed = TRUE,
                             p("We encourage you to check out the following links: "),
                             p(tags$a(href="http://healthyweight.health.gov.au/wps/portal/Home/helping-hand/different-needs/for-parents-and-guardians/healthy-eating-for-children/how%20much%20should%20children%20eat/how%20much%20should%20children%20eat/!ut/p/a1/lZDLDoJADEW_hQ8gnRnLDC55hUeEGFkgsyHDyxABWRj9fdEYdkjsqk1u77ktSDiDHNWju6h7dxtV_54lLwLmeZQhDX0eOSQUZmLGCWWpQMggt0GeWN3jEyTIqepqyCtTKV6qVq-YgToS1uply7hOBapq19SKNjh757M3WSmLbKPlR-L4VoDiMO-gyUjo2oEr9jEhIf8KfiDyOYNYhSQI6Z9HRZupneVdmbu003AtjTY9DkMRd5amvQDAa2lv/dl5/d5/L2dBISEvZ0FBIS9nQSEh/",
                                      target="_blank",
                                      "How much should children eat?"), tags$br(),
                               tags$a(href="https://www.betterhealth.vic.gov.au/health/healthyliving/childrens-diet-fruit-and-vegetables",
                                      target="_blank",
                                      "Children's diet - fruit and vegetables?")
                               )
                             )
                       )
                  )
                ),
        
        # victorian children tab
        tabItem("vic", h3("Explore how Victorian children are doing!"), tags$br(),
                fluidRow(
                  column(width=4, offset=1,
                         box(title="", status="warning", width=NULL,
                             selectInput("choice", label="Select one:",
                                         choices=c("Proportion of overweight and underweight children"="vic_weight",
                                                   "Proportion of children getting required exercise"="vic_pa"
                                                   #"Fruit & Vegetable Intake"="vic_fv"
                                                   )),
                             selectInput("filter", label="Filter by:",
                                         choices=c("Area"="area",
                                                   "Gender"="gender",
                                                   "Age"="age")),
                             conditionalPanel(condition="(input.choice=='vic_pa' || input.choice=='vic_fv') && input.filter=='area'",
                                              selectInput("area_filter", label="Filter by:",
                                                          choices=c("Overall"="overall",
                                                                    "Metropolitan"="metro",
                                                                    "Regional"="reg",
                                                                    "Victoria (all)"="vic"))
                                              ),
                             # conditionalPanel(condition="input.choice=='vic_fv'",
                             #                  selectInput("intake", "Choose food type:",
                             #                              choices=c("Fruit Intake"="fruit",
                             #                                        "Vegetable Intake"="vege",
                             #                                        "Both"="both"))),
                             p("Data from VCAMS (2014)")
                             ),
                         infoBoxOutput("info", width=NULL)
                         ),
                  column(width=6, style="padding-right: 4rem;",
                         box(title="", status="warning", width=NULL,
                             withSpinner(plotlyOutput("plot"))),
                         box(title="Did you know?", status="warning", width=NULL, collapsible = TRUE, collapsed = TRUE,
                             infoBox(width=NULL, title="",
                                     value=p("Regular physical activity has many benefits for children and young people.", tags$br(),
                                             "There's also growing evidence that suggests physical activity during the early period of 
                                             brain development can positively impact cognitive development, attention
                                             regulation and memory performance!", tags$br(),
                                             tags$b("Head on over to the next tab to find out more!"), style="font-size: 80%"),
                                     icon=icon("quote-left"), 
                                     color="red")
                             )
                       )
                  )
                
                ),
        # importance of PA tab
        tabItem("pa", h3("60 minutes per day = Unlimited Possibilities"), tags$br(),
                fluidRow(
                  column(width=6, offset=3,
                         fluidRow(infoBox(h2("Health Benefits"), width=NULL, icon=icon("stethoscope"), fill=TRUE, color="blue",
                                          p("- Lower rates of obesity", tags$br(),
                                            "- Stronger muscles and bones", tags$br(),
                                            "- Lower risk of developing type 2 diabetes", tags$br(),
                                            "- Lower blood pressure and blood cholesterol levels", tags$br(),
                                            "- Stronger immune system", 
                                            style="font-weight: normal; font-size: 75%; line-height: 1.3;")
                                          )
                                  ),
                         fluidRow(infoBox(h2("Psychological"), width=NULL, icon=icon("heart"), fill=TRUE, color="maroon",
                                          p("- Reduces symptoms of anxiety and depression", tags$br(),
                                            "- Encourages a positive attitude", tags$br(),
                                            "- Enhances self-esteem", tags$br(),
                                            "- Increases concentration and alertness", tags$br(),
                                            "- Develops leadership skills", 
                                            style="font-weight: normal; font-size: 75%; line-height: 1.3;")
                                          )
                                  ),
                         fluidRow(infoBox(h2("Mental Benefits"), width=NULL, icon=icon("graduation-cap"), fill=TRUE, color="olive",
                                          p("- Greater rates of activity are associated with higher test scores in reading and math", tags$br(),
                                            "- Creates more efficient white matter and brain connectivity", tags$br(),
                                            "- Aerobic activities increases the size of essential brain structures and number of neural connections", tags$br(),
                                            style="font-weight: normal; font-size: 75%; line-height: 1.3;")
                                          )
                                  )
                         )
                  # column(width=5, 
                  #        infoBox(h4("What are you waiting for?"),
                  #                p("Go on and click that button in the upper right hand corner!", tags$br(),
                  #                  "Start a healthier lifestyle for you and your child", tags$i("today."),
                  #                  style="font-weight: normal; font-size: 80%"),
                  #                width=NULL, 
                  #                icon = icon("arrow-up"),
                  #                color="black")
                  #        
                  #        )
                  )
                
                )
      )
    )
  )
))
