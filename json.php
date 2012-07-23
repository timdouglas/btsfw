<?php

  //hosted at http://stuff.timdouglas.co.uk/bts/fw.php

  class Act
  {
    private $name;
    private $time_on;
    private $time_off;

    public function __construct($name, $on, $off)
    {
      $this->name = $name;
      $this->time_on = $on;
      $this->time_off = $off;
    }

    public function getJsonForEncode()
    {
      $act = array(
        "act" => $this->name,
        "on" => $this->time_on,
        "off" => $this->time_off);

      return $act;
    }
  }


  $act1 = new Act("house dj", mktime(21, 0, 0, 9, 24, 2012), mktime(22, 45, 0, 9, 24, 2012));
  $act2 = new Act("launch", mktime(22, 55, 0, 9, 24, 2012), mktime(23, 0, 0, 9, 24, 2012));
  $act3 = new Act("chip shop boys", mktime(23, 0, 0, 9, 24, 2012), mktime(23, 40, 0, 9, 24, 2012));
  $act4 = new Act("random dj", mktime(23, 45, 0, 9, 24, 2012), mktime(0, 30, 0, 9, 25, 2012));
  $act5 = new Act("chip shop boys", mktime(0, 45, 0, 9, 25, 2012), mktime(1, 30, 0, 9, 25, 2012));
  $act6 = new Act("house dj", mktime(1, 40, 0, 9, 25, 2012), mktime(2, 0, 0, 9, 25, 2012));

  $acts = array($act1->getJsonForEncode(), $act2->getJsonForEncode(), 
    $act3->getJsonForEncode(), $act4->getJsonForEncode(), $act5->getJsonForEncode(), $act6->getJsonForEncode());


  $info = array(
    "acts" => $acts,
    "notes" => "EM can write something here if they wish");

  echo json_encode($info);

?>

