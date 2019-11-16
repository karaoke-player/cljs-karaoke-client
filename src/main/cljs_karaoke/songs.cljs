(ns cljs-karaoke.songs
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.string :as str]
            [cljs-karaoke.subs :as s]
            [cljs-karaoke.events :as events]
            [cljs-karaoke.events.song-list :as song-list-events]
            [cljs-karaoke.events.songs :as song-events]
            [cljs-karaoke.remote-control.commands :as cmds]
            [cljs-karaoke.events.http-relay :as remote-events]
            [re-frame.core :as rf :include-macros true]
            [cljs-karaoke.audio :as aud]
            [cljs-karaoke.lyrics :refer [preprocess-frames]]
            [cljs.core.async :as async :refer [<! >! chan go go-loop]]))
(def song-list
  ["Aaron Tippin-aint nothin wrong with the radio aaron tippon"
   "ABBA-ABBA   Angel Eyes"
   "ABBA-ABBA   Dancing Queen"
   "ABBA-ABBA   Fernando"
   "ABBA-ABBA   Knowing Me Knowing You"
   "ABBA-ABBA   Lay All Your Love On Me"
   "ABBA-ABBA   Money Money Money"
   "ABBA-ABBA   Super Trouper"
   "ABBA-ABBA   Take A Chance On Me"
   "ABBA-ABBA   Thank You For The Music"
   "ABBA-ABBA   The Winner Takes It All"
   "ABBA-ABBA   Voulez Vous"
   "ABBA-dancing queen abba"
   "ABBA-Fernando"
   "ABBA-thank you for the music abba"
   "Ace of Base-Ace Of Base   All That She Wants"
   "Ace of Base-Ace Of Base   Don't Turn Around"
   "Ace of Base-Ace Of Base   Lucky Love"
   "Ace of Base-all that she wants ace of base"
   "Ace of Base-dont turn around ace of base"
   "Ace of Base-the sign ace of base"
   "africa"
   "againstallodds"
   "Aha-AHA   Take On Me"
   "Air Supply-Air Supply   All Out Of Love"
   "Alanis Morissette-head over feet alanis morissette"
   "Alanis Morissette-Head over Feet"
   "Albert Hammond-It never rains in southern california"
   "Allanah Myles-Alanah Myles   Black Velvet"
   "Alphaville-Alphaville   Big In Japan"
   "amazing grace"
   "amie"
   "Amy Grant-baby baby amygrant"
   "Andrew Lloyd Weber-any dream will do andrew lloyd webber"
   "Andy Williams-Impossible Dream"
   "Animals-Animals House Of The Rising Sun"
   "Animals-dont let me be misunderstood animals"
   "Anka-diana paul anka"
   "anotherdayinparadise1"
   "aquarius"
   "aubrey"
   "auld lang syne"
   "Babyface-Exhale"
   "Backstreet Boys-Back Street Boys   Everybody"
   "bad"
   "Bangles-Bangles The   Eternal Flame"
   "Bangles-Bangles The   Manic Monday"
   "Bangles-eternal flame bangles"
   "Bangles-Eternal Flame"
   "Barbara Streisand-evergreen barbara sreisland"
   "Barbara Streisand-Evergreen"
   "Barry Manilow-Barry Manilow   Copacabana"
   "Barry Manilow-copacabana barry manilow"
   "Barry Manilow-I write the songs"
   "Beach Boys-california girls beachboys"
   "Beach Boys-fun fun fun beach boys"
   "Beach Boys-Fun Fun Fun"
   "Beach Boys-good vibrations beach boys"
   "Beach Boys-Good Vibrations"
   "Beach Boys-help me rhonda beachboys"
   "Beach Boys-Help Me Rhonda"
   "Beatles-all you need is love beatles"
   "Beatles-and i love her beatles"
   "Beatles-Beatles The   Yesterday"
   "Beatles-cant buy this love beatles"
   "Beatles-come together beatles"
   "Beatles-day in the life beatles"
   "Beatles-eight day a week beatles"
   "Beatles-Eight Days A Week"
   "Beatles-Eleanor Rigby "
   "Beatles-eleanor rigby beatles"
   "Beatles-Fixing a Hole"
   "Beatles-Free As A Bird"
   "Beatles-get back beatles"
   "Beatles-Get Back"
   "Beatles-getting better beatles"
   "Beatles-Getting Better"
   "Beatles-girl beatles"
   "Beatles-good moring good morning beatles"
   "Beatles-Good Morning Good Morning"
   "Beatles-goodnight beatles"
   "Beatles-Goodnight"
   "Beatles-Hard Days Night"
   "Beatles-help beatles"
   "Beatles-Help"
   "Beatles-here comes the sun beatles"
   "Beatles-Here Comes the Sun"
   "Beatles-here there and everywhere beatles"
   "Beatles-Here there Everywhere"
   "Beatles-Hey Jude"
   "Beatles-Honey Pie"
   "Beatles-I Am the Walrus"
   "Beatles-I saw her standing there"
   "Beatles-I will"
   "Beatles-If I fell"
   "Beatles-the night before beatles"
   "Beatles-ticket to ride beatles"
   "Beautiful South-Beautiful South   Don't Marry Her..."
   "beautybeast"
   "Bee Gees-Bee Gees The   How Deep Is Your Love"
   "Bee Gees-Bee Gees The   Stayin' Alive"
   "Bee Gees-Bee Gees The   Tradegy"
   "Bee Gees-How Deep Is Your Love"
   "Bee Gees-It Started as a joke"
   "Bee Gees-to love somebody beegees"
   "Bee Gees-tragedy beegees"
   "Belinda Carlisle-heaven is a place on earth belinda carlisle"
   "bennyandthejets"
   "bettydaviseyes"
   "Bill Haley-Bill Hailey & The Comets   Rock Around The Clock"
   "Billy Idol-hotcity"
   "Billy Joel-Honesty"
   "Billy Joel-It's Still Rock and Roll to me"
   "Billy Joel-pianoman"
   "Blondie-heart of glass blondie"
   "Bob Dylan-dont think twice its allright bob dylan"
   "Bob Marley-Bob Marley   No Woman No Cry"
   "Bob Marley-I shot the sherriff"
   "Bob Marley-Is this love"
   "Bobby McFerrin-Bobbie McFerrin   Don't Worry"
   "Bobby McFerrin-dont worry be happy robbie mcferrin"
   "bohemianrhapsody"
   "Boney-happy song boney m"
   "Bonnie Tyler-It's a heartacke"
   "boogiewonderland"
   "Boyzone-Boyzone   Love Me For A Reason"
   "Bread-guitar man bread"
   "Bread-Guitar Man"
   "brickwall"
   "brokenwings"
   "Bruce Springstein-dancing in the dark bruce springsteen"
   "Bruce Springstein-I'm On Fire"
   "Bryan Adams-all for love bryanadams rodstewart sting"
   "Bryan Adams-Have you ever loved a woman"
   "Bryan Adams-have you ever really loved a woman bryan adams"
   "Bryan Adams-heaven bryan adams"
   "Bryan Adams-Heaven"
   "BTO-takin care of business bachman turner overdrive"
   "Buddy Holly-Its so easy"
   "Buddy Holly-thatll be the day buddy holly"
   "Byrds-Eight Miles High"
   "C n C Music Factory-C C & The Music Factory   Everybody Dance Now"
   "C n C Music Factory-Everybody Dance Now"
   "californiadreaming"
   "candle2"
   "candle3"
   "candleinwind"
   "cantwaittobeking"
   "canyoufeelthelove"
   "Carol King-I feel the earth move"
   "Carpenters-close to you carpenters"
   "Carpenters-top of the world carpenters"
   "Cars-drive cars"
   "Celine Dion-think twice celine dion"
   "Celine Dion-to love you more celine dion"
   "Chaka Khan-I feel for you"
   "Chicago-after the love has gone chicago"
   "Chicago-hard to say im sorry chicago"
   "Chicago-Hard To Say Im Sorry"
   "Chris DeBrugh-Chris DeBurgh   Lady In Red"
   "Chris Rhea-Chris Rea   Fool If You Think It's Over"
   "circleoflife2"
   "Cole Potter-I get a kick out of you"
   "colorsofthewind"
   "Commodores-Easy Like Sunday Morning"
   "Coolio-Coolio   Gangsters Paradise"
   "Coolio-Gangstas Paradis"
   "Coolio-gangstas paradise coolio"
   "Cranberries-twenty one cranberries"
   "Cranberries-zombie cranberries"
   "Credance Clearwater Revival-Fortunate Sone"
   "Credance Clearwater Revival-Have you ever seen the rain"
   "Credance Clearwater Revival-Hey Tonight"
   "Cyndi Lauper-Cyndi Laupa   Girls Just Wanna Have Fun"
   "Cyndi Lauper-Girls Just Wanna Have Fun"
   "Cyndi Lauper-girlsfun cyndi lauper"
   "Cyndi Lauper-timeaft"
   "Dave Edmunds-I hear you knocking"
   "daybyday"
   "dayinthelife"
   "Dean Martin-Dean Martin   That's Amore"
   "dec63 2"
   "Def Leppard-two steps behind def leppard"
   "delays"
   "Dexy's Midnight Runners-Dexy's Midnight Runners   Come On Eileen"
   "diary"
   "Dione Warwick-thats what friends are for dionne warwick"
   "Disney-bare necessities disneys jungle book"
   "Disney-I just can't wait to be king"
   "Don Henley-dirty laundry don henley"
   "Don McClean-american pie donmcclean"
   "Don McClean-and i love you so donmcclean"
   "Donna Summer-I feel love"
   "Dr Hook-Dr Hook   When You're In Love With A Beautiful Woman"
   "dustinthewind"
   "Eagles-desperado eagles"
   "Eagles-Hotel California"
   "Eagles-tequila sunrise eagles"
   "Earth Wind and Fire-Fantasy"
   "easylover"
   "El Consoro-El Chacacha Del Tren"
   "Elton John-can you feel the love tonight eltonjohn"
   "Elton John-cand"
   "Elton John-candle in the wind eltonjohn"
   "Elton John-circle of life eltonjohn"
   "Elton John-dont go breaking my heart elton john"
   "Elton John-the one elton john"
   "ELVIS Are you sincere"
   "ELVIS Don't cry daddy"
   "ELVIS Fool"
   "ELVIS Good luck charm"
   "ELVIS Heartbreak hotel"
   "ELVIS Just tell her Jim said hello"
   "ELVIS Puppet on a string"
   "ELVIS Separate ways"
   "Elvis-all shook up elvis"
   "Elvis-are you lonesome tonight elvis"
   "Elvis-cant help falling in love elvis"
   "Elvis-dont be cruel elvis"
   "Elvis-hard headed woman elvis"
   "Elvis-Hard Headed Woman"
   "Elvis-Hound Doge"
   "Elvis-teddy bear elvis"
   "Erasure-a little respect erasure"
   "Erasure-always erasure"
   "Eric Clapton-bad love eric clapton"
   "Eric Clapton-change the world eric clapton"
   "Eric Clapton-tears in heaven eric clapton"
   "et"
   "Europe-final countdown europe"
   "Europe-Final Countdown"
   "Europe-Final Countdown2"
   "Europe-Final Countdown3"
   "Eurythmics-don't ask me why"
   "Eurythmics-here comes the rain again"
   "Eurythmics-it's all right"
   "Eurythmics-love is a stranger"
   "Eurythmics-missionary man"
   "Eurythmics-sisters are doi themselves"
   "Eurythmics-sweet dreams"
   "Eurythmics-there must be an angel eurythmics"
   "Eurythmics-there must be an angel"
   "Eurythmics-would i lie to you"
   "evergreen"
   "Everly Brothers-all i have to do is dream everly bros"
   "Everly Brothers-Everly Brothers The   All I Have To Do"
   "everybreath"
   "Everything But The Girl-Everything But The Girl   Missing"
   "everythingiown"
   "everythingsallright"
   "fieldsofgold"
   "fireandrain"
   "Fleetwood Mac-Gold Dust Woman"
   "fm"
   "forallweknow"
   "Foreigner-Foreigner   I Want To Know What Love Is"
   "Foreigner-I wanna know what love is"
   "Foreigner-I wanna know what love is2"
   "Four Seasons-Four Seasons The   December 1963"
   "frozen"
   "funeralkaraoke"
   "Garbage-Garbage   Stupid Girl"
   "Garth Brooks-Friends in low places"
   "Gary Numan-Gary Numan   Cars"
   "Genesis-abacab genesis"
   "Genesis-firth of fith genesis"
   "Genesis-fixing a hole genesis"
   "Genesis-Genesis   Invisible Touch"
   "Genesis-Genesis   Land Of Confusion"
   "Genesis-I can't dance"
   "Genesis-In Too Deep"
   "George Benson-In you eyes"
   "George Strait-ace in the hole george strait"
   "Gerry Raferty-ferry cross the mersey gerry and pacemakers"
   "Gerry Raferty-Gerry Raferty   Baker Street"
   "Gibson Brothers-Gibson Brothers The   Que Sera Mi Vida"
   "Glen Campbell-try a little kindness glen campbell"
   "Gloria Estefan-everlasting love gloria estefan"
   "Gloria Estefan-Get on your feet"
   "Gloria Estefan-Gloria Estefan   Can't Stay Away From You"
   "greasemegamix"
   "groovykindoflove"
   "Guess Who-american woman guesswho"
   "Harry Chapin-cats in the cradle1"
   "Harry Chapin-cats in the cradle2"
   "haveyouseentherain"
   "healtheworld"
   "Hermans Hermits-end of the world hermans hermits"
   "hero"
   "heyjude"
   "heyyou"
   "Hollies-he aint heavy hollies"
   "Huey Lewis and the News-Hip to be Square"
   "Human League-dont you want me huma league"
   "Human League-dontyouw"
   "Human League-Human League   Don't You Want Me"
   "icantdance"
   "idontknowhow"
   "if"
   "illalways"
   "illbethere"
   "imagine"
   "imnotinlove"
   "intheairtonight"
   "itdontmattertome"
   "itsraining"
   "iwannadance"
   "James Taylor-carolina in my mind james taylor"
   "James Taylor-fire and rain james taylor"
   "James Taylor-Fire and Rain"
   "jcsuperstar"
   "Jean Paul Young-Jean Paul Young   Love Is In The Air"
   "Jesus Christ Superstar-Heaven on thier minds"
   "Jesus Christ Superstar-Hosanna"
   "Jim Reeves-he ll  have to go jim reeves"
   "Jim Reeves-He'll Have to Go"
   "Jimmy Page-Good Times Bad Times"
   "Jimmy Page-I fall to pieces"
   "Jimmy Page-Immigrant song"
   "John Denver-annies song john denver"
   "John Denver-count"
   "John Lennon-happy xmas war is over john lennon"
   "John Lennon-Imagine"
   "John Lennon-In My Lifr"
   "John Mellencamp-Hurts So Good"
   "John Michael Montgomery-be my baby tonight john michael montgomery"
   "John Williamson-true blue john williamson"
   "Johnny Rivers-RiversJohnny Memphis"
   "Johnny Rivers-RiversJohnny PoorSideOfTown3"
   "Johnny Rivers-RiversJohnny Rockin'Pnuemonia"
   "Johnny Rivers-RiversJohnny SecretAgentMan5"
   "Johnny Rivers-RiversJohnny SeventhSon"
   "Johny Paycheck-take this job and shove it johnny paycheck"
   "Julio Iglesias-El Dia Que Me Quieras "
   "jump"
   "justcantstoplovingyou"
   "Katrina And the Waves-Katrina & The Waves   Walking On Sunshine"
   "KC and The Sunshine Band-thats the way i like it kc and the sunshine band"
   "killingmesoftly"
   "Kim Karnes-Kim Karnes   Betty Davis Eyes"
   "Kiss-I was made for loving you"
   "Kool and the Gang-Kool & The Gang   Ladies Night"
   "ladyinred"
   "Leanne Rhymes-RimesLeanne Can'tFightTheMoonlight"
   "Leanne Rhymes-RimesLeanne Can'tFightTheMoonlight1"
   "Led Zepplin-good times bad times led zeppelin"
   "letitbe"
   "Lionel Richie-endless love lionel richie"
   "Lionel Richie-Endless Love"
   "Lionel Richie-Endless Love2"
   "Lionel Richie-hello lionel richie"
   "Lionel Richie-Hello"
   "Lionel Richie-Lionel Ritchie   Easy"
   "Lionel Richie-Lionel Ritchie   Hello"
   "Lionel Richie-RichieLionel Angel"
   "Lionel Richie-RichieLionel Angel1"
   "Lionel Richie-RichieLionel Angel3"
   "Lionel Richie-RichieLionel Easy"
   "Lionel Richie-RichieLionel Hello2"
   "Lionel Richie-RichieLionel SayYouSayMe2"
   "Lisa Stansfield-all around the world lisa stansfield"
   "Lisa Stansfield-Lisa Stansfield   All Around The World"
   "livetotell"
   "logicalsong"
   "longtrainrunnin"
   "longwayhome"
   "losefaith"
   "losefaith2"
   "Lulu-to sir with love lulu"
   "Lynard Skynard-Gimme Three Steps"
   "Madonna-crazy for you madonna"
   "Madonna-dont cry for me argentina madonna"
   "Madonna-fever madonna"
   "Madonna-Fever"
   "Madonna-frozen"
   "Madonna-Holiday"
   "Madonna-Into the Groove"
   "Madonna-Madonna   Into The Groove"
   "Madonna-Madonna   Like A Virgin"
   "Madonna-Madonna   Papa Don't Preach"
   "Madonna-Madonna   Ray Of Light"
   "Mariah Carey-always be my baby mariah carey"
   "Mariah Carey-anytime you need a friend mariah carey"
   "Mariah Carey-dream lover mariah carey"
   "Mariah Carey-hero mariah carey"
   "Mariah Carey-I'll be there"
   "Marty Robbins-El Paso"
   "Meatloaf-bat out of hell meatloaf"
   "Meatloaf-two out of three aint bad meatloaf"
   "Men At Work-down under men at work"
   "Michael Bolton-How Am I Supposed To Live"
   "Michael Jackson-bad mjackson"
   "Michael Jackson-the way you make me feel mjackson"
   "Midnight Oil-beds"
   "moneyfornothing"
   "Monkeys-day dream believer monkees"
   "Monkeys-I'm a Believer"
   "Movie Themes-time warp rocky horror show"
   "Mr Big-to be with you mr big"
   "Neil Diamond-america neildiamond"
   "Neil Diamond-crunchy granola suite neil diamond"
   "Neil Young-heart of gold neil young"
   "Neil Young-Heart of Gold"
   "Nirvana-alll apologies nirvana"
   "Nirvana-dumb nirvana"
   "Nirvana-heart shaped box nirvana"
   "Nirvana-Heart Shaped Box"
   "niws"
   "oldtimerock"
   "Olivia Newton John-Have you ever been mellow"
   "Olivia Newton John-have you never been mellow olivia newton john"
   "Olivia Newton John-He aint heavy hes my brother"
   "Olivia Newton John-Hopelessly Devoted To You"
   "oppositesattract"
   "Otis Redding-dock of the bay otis redding"
   "paradise"
   "Patsy Cline-crazy patsy cline"
   "Patsy Cline-I'm Sorry"
   "Paul McArtney-another day paul mccartney"
   "Paul McArtney-band on the run paul mccartney"
   "Paul McArtney-ebony and ivory paul mccartney stevie wonder"
   "Paul McArtney-Ebony and Ivory"
   "Paul McArtney-goodnight tonight paul mccartney"
   "Paul McArtney-Goodnight Tonight"
   "Paul Simon-hazy shade of winter paul simon"
   "Paul Young-every time you go away paul young"
   "Paul Young-Every Time You Go Away"
   "Peggy Lee-fever peggy lee"
   "Peter Paul and Mary-I Dig Rock and Roll"
   "Phil Collins-against all odds philcollins"
   "Phil Collins-another day in paradise phil collins"
   "Phil Collins-do you remember phil collins"
   "Phil Collins-easy lover phil collins"
   "Phil Collins-Easy Lover"
   "Phil Collins-groovy kind of love phil collins"
   "Phil Collins-Groovy Kind of Love"
   "Phil Collins-Groovy Kind of Love2"
   "Pink Floyd-another brick in the wall pink floyd"
   "Pink Floyd-Hey You"
   "Pink Floyd-Hey You2"
   "Pink Floyd-High Hopes"
   "Police-cant stand losing you police"
   "Police-dont stand so close to me police"
   "Police-every breath you take police"
   "Police-Every Breath You Take"
   "Prince-cream prince"
   "Pseudo Echo-funky town pseudo echo"
   "Pseudo Echo-Funky Town"
   "Queen-crazy little called love queen"
   "Queen-friends will be friends queen"
   "Queen-Friends Will Be Friends"
   "Queen-hard days night queen"
   "Queen-these are the days of our lives queen"
   "Rascals-good lovin the rascals"
   "Rascals-Goodlovin"
   "Ray Charles-georgia on my mind raycharles"
   "Ray Charles-Georgia On My Mind"
   "Ray Parker Jr-Ghost Busters"
   "Ray Parker Jr-ghostbusters ray parker jnr"
   "rightherewaiting"
   "Robert Palmer-addicted to love robert palmer"
   "rocketman"
   "Rod Stewart-Forever Young"
   "Rolling Stones-all over now rolling stones"
   "Rolling Stones-angie rollingstones"
   "Rolling Stones-as tears go by rollingstones"
   "roundabout"
   "Roxette-crash boom bang roxette"
   "Roxette-fading like a flower roxette"
   "Roxette-Fading Like a Flower"
   "Roxette-It Must have been love"
   "Rozalla-everybodys free to feel good rozalla"
   "runaway"
   "Sarah McClachlin-whenshe"
   "Simply Red-Holding Back the Years"
   "smoothcriminal"
   "somewhere"
   "Spandau Ballet-gold spandau ballet"
   "Spin Doctors-two princes spin doctors"
   "stayingalive"
   "Steely Dan-do it again steely dan"
   "Steely Dan-FM (No Static At All)"
   "Steve Miller Band-Fly Like an Eagle"
   "Stevie Nicks-angel stevie nicks"
   "Stevie Nicks-gold dust woman stevie nicks"
   "Stevie Wonder-Isn't She Lovely"
   "Sting-englishman in ny sting"
   "Sting-fields of gold sting"
   "Sting-fragile sting"
   "Sting-Fragile"
   "Sting-free as a bird sting"
   "Sting-If I ever lose my faith in you"
   "Styx-come sail away styx"
   "summernights"
   "Supergrass-alright supergrass"
   "superstar"
   "SuperTramp-give a little bit supertramp"
   "SuperTramp-Give A Little Bit"
   "SuperTramp-It's Raining again"
   "Survivor-eye of the tiger survivor"
   "Survivor-Eye of the Tiger"
   "Suzanne Vega-toms diner suzanne vega"
   "Sweet-fox on the run sweet"
   "sweetsurrender"
   "T Rex-get it on bang a gong t rex"
   "Tears For Fears-Every Body Wants to Rule the World"
   "Tears For Fears-everybody wants to rule the world tears4fears"
   "tears"
   "Temptations-I heard it through the grapevine"
   "Temptations-Marvin Gaye   I Heard It Through The Grape Vine"
   "The Buggles-video"
   "The Byrds-eight miles high the byrds"
   "The Byrds-turn turn turn the byrds"
   "The Drifters-Drifters The   Under The Boardwalk"
   "The Hollies-Hollies The   The Air That I Breathe"
   "The Seekers-georgie girl the seekers"
   "The Wonders-that thing you do the wonders"
   "Thelma Houston-dont leave me this way thelma houston"
   "theone"
   "theraven"
   "thereyoullbe"
   "thewayyoumakemefeel"
   "thriller"
   "Tiffany-I think were alone now"
   "Tiffany-I'll follow the sun"
   "titanic"
   "titanic2"
   "Tom Jones-green green grass of home tom jones"
   "Tom Jones-Green Green Grass of Home"
   "Tom Jones-I who have nothing"
   "Tom Petty-Into the great wide open"
   "topoftheworld"
   "Toto-africa toto"
   "Tracy Chapman-fastcar"
   "Trisha Yearwood-believemebaby[1]"
   "TV Themes-adams family tv theme"
   "TV Themes-cheers theme"
   "TV Themes-Gilligans Island"
   "TV Themes-happy days tv theme"
   "TV Themes-Happy Days"
   "U2-I still haven't found what I'm looking for"
   "Van Morrison-have i told you lately van morrison"
   "Vanessa Williams-colors of the wind vanessa wms"
   "Village People-go west village people"
   "Village People-Go West"
   "Vince Gill-Give Me One More Chance"
   "vogue1"
   "wannaknow"
   "weveonlyjustbegun"
   "whatgoesup"
   "Whitesnake-again on my own whitesnake"
   "Whitesnake-Here I go Againg"
   "Whitney Houston-didnt we almost have at all whitney houston"
   "Whitney Houston-Greatest Love of All"
   "Whitney Houston-I wanna dance with somebody"
   "Whitney Houston-I will always love you"
   "wholenewworld2"
   "windbeneath"
   "wouldntitbenice"
   "wrapped"
   "wsp"
   "yellowbrickroad"
   "ymca"
   "youarenot"
   "younevergavemeyourmoney"
   "yoursong2"
   "yoursong3"
   "ZZ Top-ZZ Top   Gimme All Your Lovin'"])

(defn song-title [name]
  (-> name
      (str/replace #"_" " ")))
      ;; (str/replace #"-" " ")))

(def song-titles
  (map song-title song-list))

(def song-map
  (->> (map vector song-list song-titles)
       (into {})))

(defn song-table-pagination []
  (let [song-count (count song-list)
        current-page (rf/subscribe [::s/song-list-current-page])
        page-size (rf/subscribe [::s/song-list-page-size])
        filter-text (rf/subscribe [::s/song-list-filter])
        page-offset (rf/subscribe [::s/song-list-offset])
        next-fn #(rf/dispatch [::song-list-events/set-song-list-current-page (inc @current-page)])
        prev-fn #(rf/dispatch [::song-list-events/set-song-list-current-page (dec @current-page)])]
    (fn []
      [:nav.pagination {:role :navigation}
       [:a.pagination-previous {:on-click #(when (pos? @current-page) (prev-fn))
                                :disabled (if-not (pos? @current-page) true false)}
        "Previous"]
       [:a.pagination-next {:on-click #(when (> (- song-count @page-offset)
                                                @page-size)
                                         (next-fn))
                            :disabled (if-not (> (- song-count @page-offset)
                                                 @page-size)
                                        true
                                        false)}
        "Next"]])))
(defn song-filter-component []
  (let [filt (rf/subscribe [::s/song-list-filter])]
    [:div.field>div.control.has-icon
     [:input.input.is-primary
      {:value @filt
       :name "filter-input"
       :on-change #(rf/dispatch [::song-list-events/set-song-filter
                                 (-> % .-target .-value)])}]
     [:span.icon
      [:i.fas.fa-search]]]))
(defn song-table-component
  []
  (let [song-count (count song-list)
        current-page (rf/subscribe [::s/song-list-current-page])
        page-size (rf/subscribe [::s/song-list-page-size])
        filter-text (rf/subscribe [::s/song-list-filter])
        page-offset (rf/subscribe [::s/song-list-offset])
        remote-control-enabled? (rf/subscribe [:cljs-karaoke.subs.http-relay/remote-control-enabled?])]
    [:div.card.song-table-component
     [:div.card-header]
     [:div.card-content
      [song-filter-component]
      [song-table-pagination]
      [:table.table.is-fullwidth.song-table
       [:thead
        [:tr
         [:th "Song"]
         [:th]
         (when @remote-control-enabled?
           [:th])]]
       [:tbody
        (doall
         (for [name (->> (keys song-map)
                         (filter #(clojure.string/includes?
                                   (clojure.string/lower-case %)
                                   (clojure.string/lower-case @filter-text)))
                         (sort)
                         (drop @page-offset)
                         (take @page-size) ;(vec (sort (keys song-map)))
                         (into []))
                :let [title (get song-map name)]]
           [:tr {:key name}
            [:td title]
            [:td [:a
                  {:href (str "#/songs/" name)}
                  ;; :on-click #(select-fn name)}
                  [:i.fas.fa-play]]]
                  ;; "Load song"]]
            (when @remote-control-enabled?
              [:td
               [:a
                {:on-click (fn []
                             (let [cmd (cmds/play-song-command name)]
                               (rf/dispatch [::remote-events/remote-control-command cmd])))}
                "Play remotely"]])]))]]
      [song-table-pagination]]]))

(defn load-song
  ([name]
   (rf/dispatch-sync [::song-events/trigger-load-song-flow name]))
  ([]
   (when-let [song @(rf/subscribe [::s/playlist-current])]
     (load-song song))))
