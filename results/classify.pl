package perltools;
use strict;
use warnings;

our %lexicalStress = ();
our %slex = ();
our %biglex = ();




## pre-load stuff
{
    local $/ = undef;
    ### main lexicon
    open my $textfile, '<', '../tools/Lexicons/BIGLEX_STRESS.txt' or die $!;
    my $lex = <$textfile>;
    close $textfile;
    while( $lex =~ /([^\s\n]+)\s*\n([W S]+)/g ){
    	$lexicalStress{$1} = $2;
    }
    ### grammatical words
    open my $textfile2, '<', '../tools/Lexicons/grammatical.txt' or die $!;
    my $gramm = <$textfile2>;
    close $textfile2;
    while( $gramm =~ /([^\s\n]+)/g ){
    	$lexicalStress{$1} = "W";
    }
    
    ### LEXICONS
    open my $slexFILE, '<', '../tools/Lexicons/slex.txt' or die $!;
    my $slexTEXT = <$slexFILE>;
    close $slexFILE;
    while( $slexTEXT =~ m/([^\n ]+)/g ){
    	$slex{$1} = 1;
    }
    open my $biglexFILE, '<', '../tools/Lexicons/BIGLEX.txt' or die $!;
    my $biglexTEXT = <$biglexFILE>;
    close $biglexFILE;
    while( $biglexTEXT =~ m/([^\n ]+)/g ){
    	$biglex{$1} = 1;
    }
}

sub inSlex{
	return exists( $slex{$_[0]} );
}
sub inBiglex{
	return exists( $biglex{$_[0]} );
}


## Functions to classify words (containing schwa, monosyllable, etc)

sub classify {
	my $mono = 0; my $monoSchwa = 0;
	my $bi   = 0; my $biSchwa   = 0;
	my $tri  = 0; my $triSchwa  = 0;
	my $quad = 0; my $quadSchwa = 0;
	
	foreach( @_ ){		
		# print "$_ contains schwa: ".numberOfSchwas($_).", number of syllables: ".numberOfSyllables($_)."\n";
		my $nSyllables = numberOfSyllables( $_ );
		my $nSchwas = numberOfSchwas( $_ );
		my $schwa = ($nSchwas>0);
		if( $nSyllables == 1 ){
			$mono ++;
			$monoSchwa += $schwa;
		} elsif( $nSyllables == 2 ){
			$bi ++;
			$biSchwa += $schwa;
		} elsif( $nSyllables == 3 ){
			$tri ++;
			$triSchwa += $schwa;
		} elsif( $nSyllables == 4 ){
			$quad ++;
			$quadSchwa += $schwa;
		}else{
			die "unknown number of syllables : $_ ($nSyllables)\n";
		}
	}

	my $total = $mono + $bi + $tri + $quad;
	print "Mono: $mono , Bi  : $bi, Tri : $tri, Quad: $quad\n";
}

sub numberOfSchwas {
	my $nb = 0;
	while( $_[0] =~ /\^/g ){ $nb++; }
	return $nb;
}

sub numberOfSyllables {
	my $stress = $lexicalStress{ $_[0] };
	my $nb = 0;
	while( $stress =~ /[WS]/g ){ $nb++; }
	return $nb;
}






1;