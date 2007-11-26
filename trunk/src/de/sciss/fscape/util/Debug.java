/*
 *  Debug.java
 *  FScape
 *
 *  Copyright (c) 2001-2007 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *  Changelog:
 */

package de.sciss.fscape.util;

import java.awt.*;
import javax.swing.*;

import de.sciss.fscape.gui.*;

public class Debug
{
	/**
	 *	Open window to view 1D float data
	 */
	public static void view( float[] data, int off, int length, String descr )
	{
		final float[] dataCopy = new float[ length ];
		
		System.arraycopy( data, off, dataCopy, 0, length );
		
		int width = 256;
		int decimF = Math.max( 1, 2 * length / width );
		int decimLen = length / decimF;
		
		float[] decim = new float[ decimLen ];
		float f1, f2, f3;
		
		f2 = Float.NEGATIVE_INFINITY;
		f3 = Float.POSITIVE_INFINITY;
		for( int i = 0, j = 0; i < decimLen; ) {
			f1 = dataCopy[ j++ ];
			for( int k = 1; k < decimF; k++ ) {
				f1 = Math.max( f1, dataCopy[ j++ ]);
			}
			decim[ i++ ] = f1;
			f2 = Math.max( f2, f1 );
			f3 = Math.min( f3, f1 );
		}
		if( Float.isInfinite( f2 )) f2 = 1f;
		if( Float.isInfinite( f3 )) f3 = 0f;

		VectorDisplay ggVectorDisplay = new VectorDisplay( decim );
		ggVectorDisplay.setMinMax( f3, f2 );
//		ggVectorDisplay.addMouseListener( mia );
//		ggVectorDisplay.addMouseMotionListener( mia );
//		ggVectorDisplay.addTopPainter( tp );
//		ggVectorDisplay.setPreferredSize( new Dimension( width, 256 )); // XXX
		JPanel displayPane = new JPanel( new BorderLayout() );
		displayPane.add( ggVectorDisplay, BorderLayout.CENTER );
		Axis haxis			= new Axis( Axis.HORIZONTAL );
		Axis vaxis			= new Axis( Axis.VERTICAL );
		Box box				= Box.createHorizontalBox();
		box.add( Box.createHorizontalStrut( vaxis.getPreferredSize().width ));
		box.add( haxis );
		displayPane.add( box, BorderLayout.NORTH );
		displayPane.add( vaxis, BorderLayout.WEST );
		
		JFrame f = new JFrame( descr );
		f.setSize( width, 256 );
		f.getContentPane().add( displayPane, BorderLayout.CENTER );
		f.setVisible( true );
	}
}
// class Debug